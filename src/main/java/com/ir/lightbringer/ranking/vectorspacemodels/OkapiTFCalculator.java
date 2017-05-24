package com.ir.lightbringer.vectorspacemodels;

import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.statistics.TermStatistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Abhishek Mulay on 5/22/17.
 */
public class OkapiTFCalculator {

    private static int averageDocumentLength = Integer.parseInt(ConfigurationManager.getConfigurationValue("average.document.length"));

    public static double okapi_tf(TermStatistics termStats) {
        int termFrequency = termStats.getTermFrequency();
        int documentLength = termStats.getDocumentFrequency();
        double okapi = termFrequency / (termFrequency + 0.5 + (1.5 * (documentLength / averageDocumentLength)));
        return okapi;
    }

    // takes in a Map of documentId, List<TermTermStatistics>
    // calculates okapi_tf for each TermStatistics object and
    // returns Map<docId, final okapi_tf> value
    public static Map<String, Double> okapi_tf(Map<String, List<TermStatistics>> docIdTermStatisticsMap) {
        Map<String, Double> docIdOkapiValuesMap = new HashMap<>();
        for (Map.Entry<String, List<TermStatistics>> entry : docIdTermStatisticsMap.entrySet()) {
            String documentId = entry.getKey();
            List<TermStatistics> termStatisticsList = entry.getValue();

            double finalOkapiScore = 0.0;
            for (TermStatistics termStats : termStatisticsList) {
                finalOkapiScore += okapi_tf(termStats);
            }
            docIdOkapiValuesMap.put(documentId, finalOkapiScore);
        }
        return docIdOkapiValuesMap;
    }

}
