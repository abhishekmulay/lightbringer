package com.ir.lightbringer.retrievalmodels;

import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.statistics.TermStatistics;

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

}
