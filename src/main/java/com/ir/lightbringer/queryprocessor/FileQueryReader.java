package com.ir.lightbringer.queryprocessor;

import com.ir.lightbringer.main.ConfigurationManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Abhishek Mulay on 5/17/17.
 */
public class FileQueryReader {
    private final List<String> stopWords = StopWordsProvider.getStopWords();
    private final String QUERY_FILE_PATH = ConfigurationManager.getConfigurationValue("query.file.path");

    public List<Query> getAllQueries() {
        List<Query> queries = new ArrayList<Query>();
        try {
            FileInputStream stream = new FileInputStream(QUERY_FILE_PATH);
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));

            String line = "";
            while ((line = br.readLine()) != null) {

                //noinspection Since15
                if (!line.isEmpty()) {
                    int queryId = Integer.parseInt(line.substring(0, line.indexOf('.')));
                    String originalQuery = line.substring(6, line.length());
                    String cleanedQuery = cleanQuery(originalQuery, this.stopWords);
//                    System.out.println("id:" + queryId + "\t query:" + originalQuery);
                    queries.add(new Query(queryId, originalQuery, cleanedQuery));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queries;
    }

    //  not doing cleaning now as we have manually cleaned queries.
    private String cleanQuery(String query, List<String> stopWords) {
//        String cleanedQuery = "";
//        String[] queryTerms = query.split(" ");
//        for (String term : queryTerms) {
//            // word should not be in stoplist
//            if(stopWords.contains(term) == false) {
//                cleanedQuery += " " + term.replaceAll("\\s+","");
//            }
//        }
//        return cleanedQuery;
        //  not doing cleaning now as we have manually cleaned queries.
        return query;
    }


    public static void main(String[] args) {
        FileQueryReader reader = new FileQueryReader();
        for (Query q : reader.getAllQueries()) {
            System.out.println(q);
        }
    }
}
