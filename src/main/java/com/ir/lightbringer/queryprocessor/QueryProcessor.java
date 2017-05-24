package com.ir.lightbringer.queryprocessor;

import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.retrievalmodels.OkapiTFCalculator;
import com.ir.lightbringer.statistics.StatisticsProvider;
import com.ir.lightbringer.statistics.TermStatistics;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by Abhishek Mulay on 5/20/17.
 */
public class QueryProcessor {

    private Map<String, List<TermStatistics>> getTermStatistics(Query query) throws IOException {
        String cleanedQuery = query.getCleanedQuery();
        String[] terms = cleanedQuery.split(" ");

        Map<String, List<TermStatistics>> docIdTermStatisticsMap = new HashMap<>();

        for (String term : terms) {
            Map<String, List<TermStatistics>> statistics = StatisticsProvider.getStatistics(term);
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

    public Map<String, Double> calculateOkapi_tf(Query query) {
        try {
            Map<String, List<TermStatistics>> termStatistics = getTermStatistics(query);
            Map<String, Double> docIdOkapiValuesMap = OkapiTFCalculator.okapi_tf(termStatistics);
            return docIdOkapiValuesMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new UnsupportedOperationException("Could not calculate Okapi values for query.");
    }

    public String prettyPrintMap(Map<String, Double> map) {
        StringBuffer buffer = new StringBuffer();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            buffer.append(key).append("\t").append(value).append('\n');
        }
        return buffer.toString();
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(new File("output.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        long timeAtStart = System.nanoTime();
        String okapiOutputFile = ConfigurationManager.getConfigurationValue("okapi.output.file");
        FileQueryReader reader = new FileQueryReader();
        List<Query> allQueries = reader.getAllQueries();

        QueryProcessor processor = new QueryProcessor();

        for (Query query : allQueries) {
            System.out.println("\n\nCalculating Okapi for: " + query.getCleanedQuery());
            Map<String, Double> docIdOkapiValuesMap = processor.calculateOkapi_tf(query);
            Map<String, Double> sortedDocIdOkapiValuesMap = sortByValue(docIdOkapiValuesMap);
            QueryResultWriter.writeQueryResultToFile(query, sortedDocIdOkapiValuesMap, okapiOutputFile);
        }

        long timeAtEnd = System.nanoTime();

        long elapsedTime = timeAtEnd - timeAtStart;
        double seconds = (double) elapsedTime / 1000000000.0;
        System.out.println("Total time taken: " + seconds / 60.0 + " minutes");
    }
}
