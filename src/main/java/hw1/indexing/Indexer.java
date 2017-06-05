package hw1.indexing;

import hw1.indexing.datareader.DataReader;
import hw1.indexing.datareader.TextSanitizer;
import hw1.main.ConfigurationManager;
import hw1.pojos.HW1Model;
import hw2.CatalogEntry;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Abhishek Mulay on 6/2/17.
 */
public class Indexer {
    final static String DATA_PATH = ConfigurationManager.getConfigurationValue("data.set.path");
    final static String TEST_DATA_PATH = ConfigurationManager.getConfigurationValue("test.data.set.path");
    final static String INVERTED_INDEX_FOLDER = ConfigurationManager.getConfigurationValue("inverted.index.files.directory");

    // get term frequency for given word in given text
    public static double getTermFrequencyinText(final String term, final String[] tokens) {
        double counter = 0;
        for (final String token : tokens) {
            if (token.equals(term))
                counter += 1;
        }
        return counter;
    }

    // get positions of term in given text
    private static List<Double> getTermPositionInText(final String term, final String[] tokens) {
        double counter = 0;
        List<Double> positions = new ArrayList<>();
        for (final String token : tokens) {
            if (token.equals(term))
                positions.add(counter);
            counter += 1;
        }
        return positions;
    }

    // for a HW1Model return a map of  Map<term, Map<docId, IndexingUnit>>
    public static Map<String, Map<String, IndexingUnit>> getTermDocIdIndexingUnitMapForModel(final HW1Model model, Map<String, Map<String, IndexingUnit>> termDocIdIndexingUnitMap) {
        String documentId = model.getDocno();
        String text = model.getText();
        String[] tokens = TextSanitizer.tokenize(text);

        for (String term : tokens) {
            if (!term.isEmpty()) {
                double tf = getTermFrequencyinText(term, tokens);
                List<Double> termPositionInText = getTermPositionInText(term, tokens);
                IndexingUnit indexingUnit = new IndexingUnit(term, tf, termPositionInText);

                if (termDocIdIndexingUnitMap.containsKey(term)) {
                    Map<String, IndexingUnit> previosuDocIdIndexingUnitMap = termDocIdIndexingUnitMap.get(term);
                    previosuDocIdIndexingUnitMap.put(documentId, indexingUnit);
                    termDocIdIndexingUnitMap.put(term, previosuDocIdIndexingUnitMap);

                } else {
                    Map<String, IndexingUnit> newDocIdIndexingUnitMap = new HashMap<>();
                    newDocIdIndexingUnitMap.put(documentId, indexingUnit);
                    termDocIdIndexingUnitMap.put(term, newDocIdIndexingUnitMap);
                }
            }
        }
        return termDocIdIndexingUnitMap;
    }

    // chops a list into non-view sublists of length chunkSize
    static <T> List<List<T>> splitIntoChunks(List<T> list, final int chunkSize) {
        List<List<T>> parts = new ArrayList<List<T>>();
        final int N = list.size();
        for (int i = 0; i < N; i += chunkSize) {
            parts.add(new ArrayList<T>(list.subList(i, Math.min(N, i + chunkSize))));
        }
        return parts;
    }

    //main
    public static void main(String[] args) throws IOException {
        // Writes output to output.txt file instead of stdout
        try {
            System.setOut(new PrintStream(new File("output.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        DataReader reader = new DataReader();
        // delete previous inverted index files before creating new index.
        deleteAllInvertedIndexAndCatalogFiles();
        ArrayList<File> dataFiles = reader.getAllDataFiles(TEST_DATA_PATH);
        // break the entire dataset files into group of chunkSize, each file has around 300 documents.
        List<List<File>> partsOfDataFiles = splitIntoChunks(dataFiles, 4);

        int fileNameCounter = 0;
        for (List<File> listOfFiles : partsOfDataFiles) {
            // for file name: ex. INVERTED_INDEX_FOLDER/1.txt
            fileNameCounter += 1;
            // <term, <docId, IndexingUnit>
            Map<String, Map<String, IndexingUnit>> termDocIdIndexingUnitMap = new HashMap<>();

            // read chunkSize files at once.
            for (File dataFile : listOfFiles) {
                List<HW1Model> models = reader.readFileIntoModel(dataFile);
                System.out.println("Reading [" + models.size() + "] documents from file [" + dataFile.getName() + "]");

                // read all document models from one single file.
                for (HW1Model model : models) {
                    Map<String, Map<String, IndexingUnit>> termDocIdIndexingUnitMapForModel = getTermDocIdIndexingUnitMapForModel(model, termDocIdIndexingUnitMap);
                    termDocIdIndexingUnitMap.putAll(termDocIdIndexingUnitMapForModel);
                }
            }
            // write result of chunkSize files Map
            String invertedIndexFilePath =  INVERTED_INDEX_FOLDER + fileNameCounter + ".txt";
            String catalogFilePath = INVERTED_INDEX_FOLDER + fileNameCounter + "_catalog.txt";
            writeTermDocIdIndexingUnitMapToFile(termDocIdIndexingUnitMap, invertedIndexFilePath, catalogFilePath);

            System.out.println("[Map<term, Map<docId, IndexingUnit>>] total entries = " + termDocIdIndexingUnitMap.size() + "\n");
        }

    }

    /////////////////////////////////////////////////////////////////////////////
    //     Writing Map<term, Map<docId, IndexingUnit>> to InvertedIndexFile   //
    ///////////////////////////////////////////////////////////////////////////

    public static void writeTermDocIdIndexingUnitMapToFile(Map<String, Map<String, IndexingUnit>> docIdTermIndexingUnitMap, String invertedIndexFilePath, String catalogFilePath) {
        StringBuffer buffer = new StringBuffer();
        Map<String, CatalogEntry> catalogEntryMap = new HashMap<>();
        int position = 0;
        int offset = 0;

        // iterate over all terms
        for (Map.Entry<String, Map<String, IndexingUnit>> entry : docIdTermIndexingUnitMap.entrySet()) {
            String term = entry.getKey();
            Map<String, IndexingUnit> docIdIndexingUnitMap = entry.getValue();
            // how many bytes in file
            position = buffer.length();

            buffer.append(term).append('=');

            // iterate over map <docId, IndexingUnit> for the term
            for (Map.Entry<String, IndexingUnit> docIdIndexingUnitEntry : docIdIndexingUnitMap.entrySet()) {
                String documentId = docIdIndexingUnitEntry.getKey();
                IndexingUnit indexingUnit = docIdIndexingUnitEntry.getValue();

                double tf = indexingUnit.getTermFrequency();
                List<Double> positionList = indexingUnit.getPosition();
                String positions = Arrays.toString(positionList.toArray());
                double df = -1.0;
                double ttf = -1.0;

                // term=docId1:tf1:df1:ttf1:[pos1, pos2, pos3];docId2:tf2:df2:ttf2:[pos1, pos2, pos3]
                buffer.append(documentId).append(':').append(tf).append(':').append(df).append(':').append(ttf).append(':').append(positions);

                // there are more than 1 documents that contain this term, add a separator ';'
                if (docIdIndexingUnitMap.size() > 1) {
                    buffer.append(';');
                }
            }
            //add new line after every term entry
            buffer.append('\n');
            offset = buffer.length() - position;

            CatalogEntry catalogEntry = new CatalogEntry(term, position, offset);
            catalogEntryMap.put(term, catalogEntry);
        }
        String allTermsFromThisFileSet = buffer.toString();
        byte[] bytes = allTermsFromThisFileSet.getBytes(StandardCharsets.UTF_8);


        //save bytes[] into file
        writeBytesToFile(bytes, invertedIndexFilePath);
        // add entry for all terms in catalog
        createCatalogFile(catalogEntryMap, catalogFilePath);
    }

    private static void createCatalogFile(Map<String, CatalogEntry> catalogEntryMap, String catalogFilePath) {
        try {
            File file = new File(catalogFilePath);
            // create if file does not exist.
            if (!file.exists())
                file.createNewFile();

            StringBuffer buffer = new StringBuffer();
            for (Map.Entry<String, CatalogEntry> termCatalogEntryEntry : catalogEntryMap.entrySet()) {
                CatalogEntry catalogEntry = termCatalogEntryEntry.getValue();
                buffer.append(catalogEntry.getTerm()).append(':').append(catalogEntry.getPosition()).append(':').append(catalogEntry.getOffset()).append('\n');
            }
            String catalogEntries = buffer.toString();
            // if file is already present, append to file.
            FileOutputStream stream = new FileOutputStream(catalogFilePath, true);
            byte[] bytes = catalogEntries.getBytes(StandardCharsets.UTF_8);
            stream.write(bytes);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeBytesToFile(byte[] bytes, String invertedIndexFilePath) {
        try {
            File file = new File(invertedIndexFilePath);
            // create if file does not exist.
            if (!file.exists())
                file.createNewFile();
            FileOutputStream stream = new FileOutputStream(invertedIndexFilePath, true);
            stream.write(bytes);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllInvertedIndexAndCatalogFiles() {
        File dir = new File(INVERTED_INDEX_FOLDER);
        File[] files = dir.listFiles();
        System.out.println("Deleting [" + files.length + "] inverted index files in " + INVERTED_INDEX_FOLDER);
        for(File file: files)
            if (!file.isDirectory())
                file.delete();
    }
}
