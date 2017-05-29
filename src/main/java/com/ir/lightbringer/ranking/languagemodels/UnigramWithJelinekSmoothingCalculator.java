package com.ir.lightbringer.ranking.languagemodels;

import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.pojos.TermStatistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Abhishek Mulay on 5/27/17.
 */
public class UnigramWithJelinekSmoothingCalculator {
    private static int vocabularySize = Integer.parseInt(ConfigurationManager.getConfigurationValue("corpus.vocabulary.size"));
    private static double lambda = 0.7;

    private static double p_jm (TermStatistics termStatistics) {
        int termFrequency = termStatistics.getTermFrequency();
        int documentLength = termStatistics.getDocumentLength();
        double ttf = termStatistics.getTtf() * 1.0;
        double score = (lambda * (termFrequency/documentLength) + (1-lambda) * (ttf/vocabularySize));
        return score;
    }

    public static Map<String, Double> lm_jm(Map<String, List<TermStatistics>> docIdTermStatisticsMap, Map<String, Double> docIdFinalLaplaceValue) {
        Map<String, Double> docIdJelinekValuesMap = new HashMap<>();

        for (Map.Entry<String, List<TermStatistics>> entry : docIdTermStatisticsMap.entrySet()) {
            String documentId = entry.getKey();
            List<TermStatistics> termStatisticsList = entry.getValue();

            double finalJelinekValue = 0.0;
            for (TermStatistics termStats : termStatisticsList) {
                double smoothedValue = p_jm(termStats);
                finalJelinekValue += Math.log(smoothedValue);
            }
            docIdJelinekValuesMap.put(documentId, finalJelinekValue);
        }
        return docIdJelinekValuesMap;
    }

}
