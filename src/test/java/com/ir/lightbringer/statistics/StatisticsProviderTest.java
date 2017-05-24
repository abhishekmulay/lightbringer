package com.ir.lightbringer.statistics;

import junit.framework.TestCase;

import java.util.Map;

/**
 * Created by Abhishek Mulay on 5/23/17.
 */
public class StatisticsProviderTest extends TestCase {

//    {
//        "_index": "ap_dataset",
//            "_type": "hw1",
//            "_id": "AP890608-0108",
//            "_score": 15.043134,
//            "fields": {
//                "term_frequency": [
//                3
//                  ],
//                "doc_frequency": [
//                5
//                  ],
//                "ttf": [
//                8
//                  ]
//            }
//    },

    public void testGetStatistics() throws Exception {
        final String testTerm = "corrupt";
//        final TermStatistics expectedStatistic = new TermStatistics();
        Map<String, TermStatistics> statistics = StatisticsProvider.getStatistics(testTerm);
        System.out.println("statistics" + statistics);
    }

}