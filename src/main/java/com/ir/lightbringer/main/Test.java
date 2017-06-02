package com.ir.lightbringer.main;

import com.ir.lightbringer.pojos.TermStatistics;
import com.ir.lightbringer.restclient.RestCallHandler;
import com.ir.lightbringer.statistics.StatisticsProvider;
import com.ir.lightbringer.util.MapUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Abhishek Mulay on 5/31/17.
 */
public class Test {

    public static void main(String[] args) {
        final String testString = "europ";
        try {
            Map<String, List<TermStatistics>> statisticsForAlgorithm = StatisticsProvider.getStatistics(testString);

            for (Map.Entry<String, List<TermStatistics>> entry :  statisticsForAlgorithm.entrySet()) {
                String documentId = entry.getKey();
                List<TermStatistics> statisticsList = entry.getValue();
                System.out.println(documentId + "\t=>\t" + statisticsList + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
