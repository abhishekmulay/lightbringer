package com.ir.lightbringer.queryprocessor;

import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.pojos.Query;
import com.ir.lightbringer.ranking.languagemodels.BM25Calculator;
import com.ir.lightbringer.ranking.languagemodels.UnigramWithLaplaceSmoothingCalculator;
import com.ir.lightbringer.restclient.DocumentIdExtractor;
import com.ir.lightbringer.util.MapUtils;
import com.ir.lightbringer.ranking.vectorspacemodels.OkapiTFCalculator;
import com.ir.lightbringer.statistics.StatisticsProvider;
import com.ir.lightbringer.pojos.TermStatistics;
import com.ir.lightbringer.ranking.vectorspacemodels.TfIdfCalculator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by Abhishek Mulay on 5/20/17.
 */
public class QueryProcessor {

    public void calculateOkapi_tf(List<Query> queryList, String okapiOutputFile) {
        for (Query query : queryList) {
            System.out.println("\n\nCalculating Okapi for: " + query.getCleanedQuery());

            // <docId, [TermStatistics]>
            Map<String, List<TermStatistics>> termStatistics = null;
            try {
                termStatistics = getTermStatistics(query);
                Map<String, Double> docIdOkapiValuesMap = OkapiTFCalculator.okapi_tf(termStatistics);
                Map<String, Double> sortedDocIdOkapiValuesMap = MapUtils.sortByValue(docIdOkapiValuesMap);
                QueryResultWriter.writeQueryResultToFile(query, sortedDocIdOkapiValuesMap, okapiOutputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void calculateTfIdf(List<Query> queryList, String tfIdfOutputFile) {
        for (Query query : queryList) {
            System.out.println("\n\nCalculating TF-IDF for: " + query.getCleanedQuery());
            try {
                Map<String, List<TermStatistics>> termStatistics = getTermStatistics(query);
                Map<String, Double> docIdTfIdfValuesMap = TfIdfCalculator.tfidf(termStatistics);
                Map<String, Double> sortedDocIdOkapiValuesMap = MapUtils.sortByValue(docIdTfIdfValuesMap);
                QueryResultWriter.writeQueryResultToFile(query, sortedDocIdOkapiValuesMap, tfIdfOutputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void calculateOkapiMb25(List<Query> queryList, String bm25OutputFile) {
        for (Query query : queryList) {
            System.out.println("\n\nCalculating Okapi BM25 for: " + query.getCleanedQuery());
            try {
                Map<String, List<TermStatistics>> termStatistics = getTermStatistics(query);
                Map<String, Double> docIdBm25ValuesMap = BM25Calculator.bm25(termStatistics, query);
                Map<String, Double> sortedDocIdBm25ValuesMap = MapUtils.sortByValue(docIdBm25ValuesMap);
                QueryResultWriter.writeQueryResultToFile(query, sortedDocIdBm25ValuesMap, bm25OutputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void calculateUnigramWithLaplaceSmoothing(List<Query> queryList, String unigramWithLaplaceSmoothingOutputFile) {
        int vocabularySize = Integer.parseInt(ConfigurationManager.getConfigurationValue("corpus.vocabulary.size"));
        DocumentIdExtractor extractor = new DocumentIdExtractor();
        Set<String> allDocumentIds = null;
        try {
            allDocumentIds = extractor.getAllDocumentIds();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Query query : queryList) {
            System.out.println("\n\nCalculating Unigram LM with Laplace smoothing for: " + query.getCleanedQuery());
            Map<String, Double> docIdFinalLaplaceValue = new HashMap<>();
            int termsInQuery = query.getCleanedQuery().split(" ").length;
            double defaultValue = termsInQuery * Math.log(1/vocabularySize);
            for (String id : allDocumentIds) {
                docIdFinalLaplaceValue.put(id, defaultValue);
            }
            try {
                Map<String, List<TermStatistics>> termStatistics = getTermStatistics(query);
                Map<String, Double> docIdUnigramValuesMap = UnigramWithLaplaceSmoothingCalculator.lm_laplace(termStatistics, docIdFinalLaplaceValue);
                Map<String, Double> sortedDocIdUnigramValuesMap = MapUtils.sortByValue(docIdUnigramValuesMap);
                QueryResultWriter.writeQueryResultToFile(query, sortedDocIdUnigramValuesMap, unigramWithLaplaceSmoothingOutputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    private Map<String, List<TermStatistics>> getTermStatistics(Query query) throws IOException {
        String cleanedQuery = query.getCleanedQuery();
        String[] terms = cleanedQuery.split(" ");

        Map<String, List<TermStatistics>> docIdTermStatisticsMap = new HashMap<>();

        for (String term : terms) {
            // get map of <docId, List[stats for terms in that docId]>
            Map<String, List<TermStatistics>> statistics = StatisticsProvider.getStatistics(term);

            // update main map with values
            for (Map.Entry<String, List<TermStatistics>> entry : statistics.entrySet()) {
                String documentId = entry.getKey();
                List<TermStatistics> statisticsForDocumentId = entry.getValue();

                if (docIdTermStatisticsMap.containsKey(documentId)) {
                    List<TermStatistics> previousTermStatistics = docIdTermStatisticsMap.get(documentId);
                    previousTermStatistics.addAll(statisticsForDocumentId);
                    docIdTermStatisticsMap.put(documentId, previousTermStatistics);
                } else {
                    docIdTermStatisticsMap.put(documentId, statisticsForDocumentId);
                }
            }
        }

        return docIdTermStatisticsMap;
    }


    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(new File("output.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        long timeAtStart = System.nanoTime();
        String okapiOutputFile = ConfigurationManager.getConfigurationValue("okapi.output.file");
        String tfIdfOutputFile = ConfigurationManager.getConfigurationValue("tfidf.output.file");
        String bm25OutputFile = ConfigurationManager.getConfigurationValue("bm-25.output.file");
        String unigramWithLaplaceSmoothingOutputFile = ConfigurationManager.getConfigurationValue("unigram.laplace.output.file");

        FileQueryReader reader = new FileQueryReader();
        List<Query> allQueries = reader.getAllQueries();

        QueryProcessor processor = new QueryProcessor();
//        processor.calculateOkapi_tf(allQueries, okapiOutputFile);
//        processor.calculateTfIdf(allQueries, tfIdfOutputFile);
//        processor.calculateOkapiMb25(allQueries, bm25OutputFile);
        processor.calculateUnigramWithLaplaceSmoothing(allQueries, unigramWithLaplaceSmoothingOutputFile);

        long timeAtEnd = System.nanoTime();
        long elapsedTime = timeAtEnd - timeAtStart;
        double seconds = (double) elapsedTime / 1000000000.0;
        System.out.println("Total time taken: " + seconds / 60.0 + " minutes");
    }
}
