package com.ir.lightbringer.queryprocessor;

import com.ir.lightbringer.retrievalmodels.OkapiTFCalculator;
import com.ir.lightbringer.statistics.StatisticsProvider;
import com.ir.lightbringer.statistics.TermStatistics;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Abhishek Mulay on 5/20/17.
 */
public class QueryProcessor {

    public Map<String, Double> getOkapiTFforQuery(Query query) {
        String cleanedQuery = query.getCleanedQuery();
        String[] terms = cleanedQuery.split(" ");
        Map<String, Map<String, Double>> docOkapiMap = calculateOkapi_tfForAllTermsInQuery(terms);

        Map<String, Double> docIdFinalOkapiMap = new HashMap<String, Double>();

        for (Map.Entry<String, Map<String, Double>> entry : docOkapiMap.entrySet()) {

            String documentId = entry.getKey();
            Map<String, Double> termOkapiMap = entry.getValue();

            double finalOkapi = 0;
            for (Map.Entry<String, Double> termEntity : termOkapiMap.entrySet()) {
                String term = termEntity.getKey();
                Double okapiValue = termEntity.getValue();
                finalOkapi += okapiValue;
            }
            docIdFinalOkapiMap.put(documentId, finalOkapi);
        }

        System.out.println( "\n\nFinal OKAPI scores: \n" + prettyPrintMap(docIdFinalOkapiMap));
        return docIdFinalOkapiMap;
    }

    // Map <docId, <term, okapi_tf>>
    public Map<String, Map<String, Double>> calculateOkapi_tfForAllTermsInQuery(String[] terms) {
        Map<String, Map<String, Double>> docOkapiMap = new HashMap<String, Map<String, Double>>();
        try {
            for (String term : terms) {

                // <docId, TermStat>
                Map<String, TermStatistics> docIdStatsMap = StatisticsProvider.getStatistics(term);

                for (Map.Entry<String, TermStatistics> entry : docIdStatsMap.entrySet()) {
                    String documentId = entry.getKey();
                    TermStatistics termStatistics = entry.getValue();
                    double okapiValue = OkapiTFCalculator.okapi_tf(termStatistics);

                    if (docOkapiMap.containsKey(documentId)) {
                        Map<String, Double> termOkapiValueMap = docOkapiMap.get(documentId);
                        Double previousValue = termOkapiValueMap.get(documentId);
                        termOkapiValueMap.put(documentId, previousValue + okapiValue);
                        docOkapiMap.put(documentId, termOkapiValueMap);
                    } else {
                        Map<String, Double> hash = new HashMap<String, Double>();
                        hash.put(documentId, okapiValue);
                        docOkapiMap.put(documentId, hash);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return docOkapiMap;
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


    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(new File("output.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileQueryReader reader = new FileQueryReader();
        List<Query> allQueries = reader.getAllQueries();
        Query query = allQueries.get(0);
        System.out.println("Finding metrices for query " + query.getCleanedQuery());

        QueryProcessor processor = new QueryProcessor();
        processor.getOkapiTFforQuery(query);

    }
}
