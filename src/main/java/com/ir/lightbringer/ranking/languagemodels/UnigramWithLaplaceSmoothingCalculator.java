package com.ir.lightbringer.ranking.languagemodels;

import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.pojos.TermStatistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Abhishek Mulay on 5/24/17.
 */
public class UnigramWithLaplaceSmoothingCalculator {

    private static int vocabularySize = Integer.parseInt(ConfigurationManager.getConfigurationValue("corpus.vocabulary.size"));

    public static double p_laplace(TermStatistics termStatistics) {
        int termFrequency = termStatistics.getTermFrequency();
        int documentLength = termStatistics.getDocumentLength();

        double score = ((termFrequency + 1.0) / (documentLength + vocabularySize)) - (Math.log(1.0 / vocabularySize));
        return score;
    }

    public static Map<String, Double> lm_laplace(Map<String, List<TermStatistics>> docIdTermStatisticsMap, Map<String, Double> docIdFinalLaplaceValue) {
        Map<String, Double> docIdOkapiValuesMap = new HashMap<>();

        for (Map.Entry<String, List<TermStatistics>> entry : docIdTermStatisticsMap.entrySet()) {
            String documentId = entry.getKey();
            List<TermStatistics> termStatisticsList = entry.getValue();

            double finalUnigramWithLaplace = 0.0;
            for (TermStatistics termStats : termStatisticsList) {
                double smoothedValue = p_laplace(termStats);
                finalUnigramWithLaplace += Math.log(smoothedValue);
            }
            docIdOkapiValuesMap.put(documentId, finalUnigramWithLaplace);
        }
        return docIdOkapiValuesMap;
    }

}
