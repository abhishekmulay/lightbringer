package hw2.evaluation;

import hw1.indexing.datareader.TextSanitizer;
import hw1.main.ConfigurationManager;
import hw2.indexing.IndexingUnit;
import hw2.search.TermSearcher;
import util.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek Mulay on 6/8/17.
 */
public class TermListTester {
    final String TEST_TERMS_FILE = ConfigurationManager.getConfigurationValue("indexing.test.terms.list");

    private static final String STEMMED_OUTPUT_FILE = ConfigurationManager.getConfigurationValue("indexing.test" + ".output.file.stemmed");
    private static final String NON_STEMMED_OUTPUT_FILE = ConfigurationManager.getConfigurationValue("indexing.test.output.file.non.stemmed");

    private static final String STEMMED_EXPECTED_OUTPUT_FILE = ConfigurationManager.getConfigurationValue("indexing.test.expected.output.file.stemmed");
    private static final String NON_STEMMED_EXPECTED_OUTPUT_FILE = ConfigurationManager.getConfigurationValue("indexing.test.expected.output.file.non.stemmed");

    private List<String> readTestTerms() {
        List<String> testTerms = new ArrayList<String>();
        try {
            InputStream content = new FileInputStream(TEST_TERMS_FILE);
            BufferedReader br = new BufferedReader(new InputStreamReader(content));
            String line = "";
            while ((line = br.readLine()) != null) {
                testTerms.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testTerms;
    }

    public void testTermsAgainstIndex(final String outputFilePath) {
        // delete previous file and create new
        File file = new File(outputFilePath);
        if (file.exists()) {
            file.delete();
        }

        List<String> testTerms = readTestTerms();
        StringBuilder builder = new StringBuilder();
        for (String term : testTerms) {
            String stemmedTerm = TextSanitizer.stem(term);
            List<IndexingUnit> indexingUnitList = TermSearcher.search(stemmedTerm);
            // word found in data set
            if (indexingUnitList.size() > 0) {
                builder.append(term).append(" ");
                builder.append(indexingUnitList.get(0).getDocumentFrequency()).append(" ");
                builder.append(indexingUnitList.get(0).getTtf()).append("\n");
            } else {
                // word not found in dataset
                builder.append(term).append(" ");
                builder.append(0).append(" ");
                builder.append(0).append("\n");
            }
        }
        String data = builder.toString();
        System.out.println(data);
        byte[] bytes = data.getBytes();
//        FileUtils.writeBytesToFile(bytes, outputFilePath);
        FileUtils.writeLineToFile(data, outputFilePath);
    }

    public void testStemmedOutputFile() {
        testTermsAgainstIndex(STEMMED_OUTPUT_FILE);
    }

    public void testNonStemmedOutputFile() {
        testTermsAgainstIndex(NON_STEMMED_OUTPUT_FILE);
    }

    public static void testSingleTerm(String term, boolean stemmingEnabled) {
        List<IndexingUnit> searchResults = null;
        final String stemmer = TextSanitizer.stem(term);
        if (stemmingEnabled) {
            searchResults = TermSearcher.search(stemmer);
        } else {
            searchResults = TermSearcher.search(term);
        }

        // print results
        for (IndexingUnit unit : searchResults) {
            System.out.println(unit.toPrettyString());
        }
        System.out.print("\nTerm=" + term + (stemmingEnabled? ", stemmed=" + stemmer : ""));
        System.out.print(", df=" + searchResults.size() + (searchResults.size() > 0?  ", ttf=" + searchResults.get(0).getTtf() : "") + "\n");
    }

    public static void main(String[] args) throws IOException {
        TermListTester termListTester = new TermListTester();
        termListTester.testStemmedOutputFile();
//
        final String expected = STEMMED_EXPECTED_OUTPUT_FILE;
        final String actual = STEMMED_OUTPUT_FILE;
        OutputAccuracy.compareFiles(expected, actual);

//        testSingleTerm("encryption", true);
    }

}
