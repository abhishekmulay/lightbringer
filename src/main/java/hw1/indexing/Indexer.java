package hw1.indexing;

import hw1.indexing.datareader.DataReader;
import hw1.indexing.datareader.TextSanitizer;
import hw1.main.ConfigurationManager;
import hw1.pojos.HW1Model;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by Abhishek Mulay on 6/2/17.
 */
public class Indexer {
    final static String DATA_PATH = ConfigurationManager.getConfigurationValue("data.set.path");
    final static String TEST_DATA_PATH = ConfigurationManager.getConfigurationValue("test.data.set.path");

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
    public static Map<String, Map<String, IndexingUnit>> getDocIdTermIndexingUnitMapForModel(final HW1Model model) {
        // docId, <term, IndexingUnit>
        Map<String, Map<String, IndexingUnit>> docIdTermIndexingUnitMap = new HashMap<>();
        String documentId = model.getDocno();
        String text = model.getText();
        String[] tokens = TextSanitizer.tokenize(text);

        for (String term : tokens) {
            if (!term.isEmpty()) {
                double tf = getTermFrequencyinText(term, tokens);
                List<Double> termPositionInText = getTermPositionInText(term, tokens);
                IndexingUnit indexingUnit = new IndexingUnit(term, tf, termPositionInText);

                Map<String, IndexingUnit> docIdIndexingUnitMap = new HashMap<>();
                docIdIndexingUnitMap.put(documentId, indexingUnit);
                docIdTermIndexingUnitMap.put(term, docIdIndexingUnitMap);
            }
        }

        return docIdTermIndexingUnitMap;
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
        ArrayList<File> dataFiles = reader.getAllDataFiles(DATA_PATH);
        // break the entire dataset files into group of chunkSize, each file has around 300 documents.
        List<List<File>> partsOfDataFiles = splitIntoChunks(dataFiles, 3);

        for (List<File> listOfFiles : partsOfDataFiles) {
            // docId, <term, IndexingUnit>
            Map<String, Map<String, IndexingUnit>> docIdTermIndexingUnitMap = new HashMap<>();

            for (File dataFile : listOfFiles) {
                List<HW1Model> models = reader.readFileIntoModel(dataFile);
                System.out.println("Reading [" + models.size() + "] documents from file [" + dataFile.getName() + "]");

                for (HW1Model model : models) {
                    Map<String, Map<String, IndexingUnit>> docIdTermIndexingUnitMapForModel = getDocIdTermIndexingUnitMapForModel(model);
                    docIdTermIndexingUnitMap.putAll(docIdTermIndexingUnitMapForModel);
                }
            }

            System.out.println("[Map<term, Map<docId, IndexingUnit>>] total entries = " + docIdTermIndexingUnitMap.size() + "\n");
        }

    }

}
