package com.ir.lightbringer.languagemodels;

import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.queryprocessor.Query;
import com.ir.lightbringer.statistics.TermStatistics;

/**
 * Created by Abhishek Mulay on 5/24/17.
 */
public class BM25Calculator {

    private final static int corpusSize = 84678;
    private final static int averageDocumentLength = Integer.parseInt(ConfigurationManager.getConfigurationValue("average.document.length"));

    public static double bm25(TermStatistics termStatistics, Query query) {

        int k1 = 0;
        int k2 = 0;
        int b = 0;

        int docFrequency = termStatistics.getDocumentFrequency();
        int termFrequency = termStatistics.getTermFrequency();
        double result = 0.0;
        for (String term : query.getCleanedQuery().split(" ")) {
            double firstLogFactor = Math.log((corpusSize + 0.5) / (docFrequency + 0.5));

        }
        return result;
    }

}
