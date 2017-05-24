package com.ir.lightbringer.queryprocessor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by Abhishek Mulay on 5/23/17.
 */
public class QueryResultWriter {

    public void writeQueryResultToFile(List<Query> queryList, Map<String, Double> docIdScoreMap, String outputFile) {
        try {
            PrintWriter writer = new PrintWriter(outputFile, "UTF-8");

            for (Query query : queryList) {
                int queryNumber = query.getQueryId();
                int rank = 0;
                String documentId = "";
                Double score = 0.0;
                for (Map.Entry<String, Double> entry : docIdScoreMap.entrySet()) {
                    documentId = entry.getKey();
                    score = entry.getValue();
                    rank += 1;

                    // <query-number> Q0 <docno> <rank> <score> Exp
                    writer.println(queryNumber + " Q0 " + documentId + " " + rank + " " + score + " Exp");
                }
                writer.print("\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
