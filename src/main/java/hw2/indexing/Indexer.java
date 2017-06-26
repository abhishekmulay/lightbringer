package hw2.indexing;

import hw1.indexing.datareader.DataReader;
import hw1.indexing.datareader.TextSanitizer;
import hw1.main.ConfigurationManager;
import hw1.pojos.HW1Model;
import hw2.search.DocumentSummaryProvider;
import util.FileUtils;
import util.ListUtils;
import util.MapUtils;

import java.io.*;
import java.util.*;

/**
 * Created by Abhishek Mulay on 6/2/17.
 */
public class Indexer {

    final static String INVERTED_INDEX_FOLDER = ConfigurationManager.getConfigurationValue("inverted.index.files.directory");
    final static String COMPLETED_FOLDER = ConfigurationManager.getConfigurationValue("completed.files.directory");
    final static boolean STEMMING_AND_STOPWORD_REMOVAL_ENABLED = Boolean.parseBoolean(ConfigurationManager.getConfigurationValue("stopwords.removal.and.stemming.enabled"));
    final static String VOCABULARY_FILE_PATH = ConfigurationManager.getConfigurationValue("vocabulary.file.path");

    private static DocumentSummaryProvider summaryProvider = new DocumentSummaryProvider();
    private static int TOTAL_DOCUMENTS_PROCESSED = 0;
    public static int INDEX_NUMBER = 0;

    // get term frequency for given word in given text
    public static int getTermFrequencyinText(final String term, final String[] tokens) {
        int counter = 0;
        for (final String token : tokens) {
            if (token.equals(term))
                counter += 1;
        }
        return counter;
    }

    // get positions of term in given text
    private static List<Integer> getTermPositionInText(final String term, final String[] tokens) {
        int counter = 0;
        List<Integer> positions = new ArrayList<>();
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
        int docIdMappingNumber = DocumentSummaryProvider.getDocIdMappingNumber(documentId);
        String text = model.getText();
        // remove stop-words and tokenize

//        String[] tokens = TextSanitizer.removeStopWordsAndTokenize(text, STEMMING_AND_STOPWORD_REMOVAL_ENABLED);
        String[] tokens =TextSanitizer.getTokens(text, STEMMING_AND_STOPWORD_REMOVAL_ENABLED);

        for (String term : tokens) {
            int tf = getTermFrequencyinText(term, tokens);
            List<Integer> termPositionInText = getTermPositionInText(term, tokens);
            int ttf = tf, df = 1;
            IndexingUnit indexingUnit = new IndexingUnit(term, documentId, docIdMappingNumber, tf, termPositionInText, ttf, df);

            if (termDocIdIndexingUnitMap.containsKey(term)) {
                Map<String, IndexingUnit> previosuDocIdIndexingUnitMap = termDocIdIndexingUnitMap.get(term);
                previosuDocIdIndexingUnitMap.put(documentId, indexingUnit);
                termDocIdIndexingUnitMap.put(term, MapUtils.sortByTF(previosuDocIdIndexingUnitMap));

            } else {
                Map<String, IndexingUnit> newDocIdIndexingUnitMap = new TreeMap<>();
                newDocIdIndexingUnitMap.put(documentId, indexingUnit);
                termDocIdIndexingUnitMap.put(term, newDocIdIndexingUnitMap);
            }
        }
        // update df and ttf for records so far
        termDocIdIndexingUnitMap = updateDocIdIndexingUnitMapValues(termDocIdIndexingUnitMap);
        return termDocIdIndexingUnitMap;
    }

    // this method will increment the tf, ttf and df counts for records
    private static Map<String, Map<String, IndexingUnit>> updateDocIdIndexingUnitMapValues(Map<String, Map<String, IndexingUnit>> termDocIdIndexingUnitMap) {
        int ttf = 0;
        int df = 0;
        for (Map.Entry<String, Map<String, IndexingUnit>> termMapEntry : termDocIdIndexingUnitMap.entrySet()) {
            String term = termMapEntry.getKey();

            ttf = 0;
            df = 0;
            for (Map.Entry<String, IndexingUnit> docIdIndexingUnitEntry : termDocIdIndexingUnitMap.get(term).entrySet()) {
                IndexingUnit unit = docIdIndexingUnitEntry.getValue();
                ttf += unit.getTermFrequency();
            }
            df = termDocIdIndexingUnitMap.get(term).size();

            for (Map.Entry<String, IndexingUnit> docIdIndexingUnitEntry : termDocIdIndexingUnitMap.get(term).entrySet()) {
                IndexingUnit unit = docIdIndexingUnitEntry.getValue();
                unit.setTtf(ttf);
                unit.setDocumentFrequency(df);
            }
        }
        return termDocIdIndexingUnitMap;
    }

    /////////////////////////////////////////////////////////////////////////////

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
            int df = 0, ttf = 0;

            StringBuilder bufferForTerm = new StringBuilder();
            // iterate over map <docId, IndexingUnit> for the term
            for (Map.Entry<String, IndexingUnit> docIdIndexingUnitEntry : docIdIndexingUnitMap.entrySet()) {
                String documentId = docIdIndexingUnitEntry.getKey();
                IndexingUnit indexingUnit = docIdIndexingUnitEntry.getValue();
                int tf = indexingUnit.getTermFrequency();
                df += 1;
                ttf += tf;
                List<Integer> positionList = indexingUnit.getPosition();
                String positions = ListUtils.toCompactString(positionList);

                int docIdMappingNumber = DocumentSummaryProvider.getDocIdMappingNumber(documentId);
                // term=df;ttf;docIdMappingNumber1:tf1:pos1,pos2,pos3];docIdMappingNumber2:tf2:pos1,pos2,pos3];docIdMappingNumber3:tf3:pos1,pos2,pos3];
                bufferForTerm.append(docIdMappingNumber).append(':').append(tf).append(':').append(positions).append(';');
            }
            bufferForTerm.append('\n');
            String docBlocks = bufferForTerm.toString();
            buffer.append(df).append(";").append(ttf).append(";").append(docBlocks);

            //add new line after every term entry
            offset = buffer.length() - position;

            CatalogEntry catalogEntry = new CatalogEntry(term, position, offset);
            catalogEntryMap.put(term, catalogEntry);
        }
        String allTermsFromThisFileSet = buffer.toString();

        //save bytes[] into file
//        FileUtils.writeBytesToFile(bytes, invertedIndexFilePath);
        FileUtils.writeLineToFile(allTermsFromThisFileSet, invertedIndexFilePath);
        // add entry for all terms in catalog
        createCatalogFile(catalogEntryMap, catalogFilePath);
    }

    public static void createCatalogFile(Map<String, CatalogEntry> catalogEntryMap, String catalogFilePath) {
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
            byte[] bytes = catalogEntries.getBytes();
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
        for (File file : files)
            file.delete();


        File folder = new File(COMPLETED_FOLDER);
        File[] completedfiles = folder.listFiles();
        System.out.println("Deleting [" + completedfiles.length + "] completed files in " + COMPLETED_FOLDER + "\n");
        for (File file : completedfiles)
            file.delete();
    }

    ///////////////////////////////////////////////////////////////////////////
    //     Writing Map<term, Map<docId, IndexingUnit>> to InvertedIndexFile   //

    //main method
    public static void runIndex(String dataPath, int fileChunkSize) throws IOException {
        DataReader reader = new DataReader();
        // delete previous inverted index files before creating new index.
        deleteAllInvertedIndexAndCatalogFiles();
        ArrayList<File> dataFiles = reader.getAllDataFiles(dataPath);
        // break the entire dataset files into group of chunkSize, each file has around 300 documents.
        List<List<File>> partsOfDataFiles = ListUtils.splitIntoChunks(dataFiles, fileChunkSize);

        Set<String> vocabulary = new HashSet<>();
        for (List<File> listOfFiles : partsOfDataFiles) {
            // for file name: ex. INVERTED_INDEX_FOLDER/1.txt
            INDEX_NUMBER += 1;
            // <term, <docId, IndexingUnit>
            Map<String, Map<String, IndexingUnit>> termDocIdIndexingUnitMap = new HashMap<>();
            int documentsProcessedInChunk = 0;
            // getCatalogAsMap chunkSize files at once.
            for (File dataFile : listOfFiles) {
                List<HW1Model> models = reader.readFileIntoModel(dataFile);
                TOTAL_DOCUMENTS_PROCESSED += models.size();
                documentsProcessedInChunk += models.size();
                System.out.println("Reading [" + models.size() + "] documents from file [" + dataFile.getName() + "]");

                // getCatalogAsMap all document models from one single file.
                for (HW1Model model : models) {
                    Map<String, Map<String, IndexingUnit>> termDocIdIndexingUnitMapForModel = getTermDocIdIndexingUnitMapForModel(model, termDocIdIndexingUnitMap);
                    termDocIdIndexingUnitMap.putAll(termDocIdIndexingUnitMapForModel);
                }
            }

            vocabulary.addAll(termDocIdIndexingUnitMap.keySet());

            // write result of chunkSize files Map
            String invertedIndexFilePath = INVERTED_INDEX_FOLDER + INDEX_NUMBER + ".txt";
            String catalogFilePath = INVERTED_INDEX_FOLDER + INDEX_NUMBER + "_catalog.txt";
            writeTermDocIdIndexingUnitMapToFile(termDocIdIndexingUnitMap, invertedIndexFilePath, catalogFilePath);

            System.out.println("Documents=[" + documentsProcessedInChunk + "], index=[" + new File(invertedIndexFilePath).getName() + "], totalDocumentsProcessed =[" + TOTAL_DOCUMENTS_PROCESSED + "], vocabulary size=["+ vocabulary.size() +"]\n");
        }
        writeVocabularyToFile(vocabulary);
    }

    private static void writeVocabularyToFile(Set<String> vocabulary) {
        StringBuilder builder = new StringBuilder();
        for (String term : vocabulary) {
            builder.append(term).append('\n');
        }
        FileUtils.writeLineToFile(builder.toString(), VOCABULARY_FILE_PATH);
    }


}
