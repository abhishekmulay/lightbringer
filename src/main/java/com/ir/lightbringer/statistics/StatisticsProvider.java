package com.ir.lightbringer.statistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.pojos.Query;
import com.ir.lightbringer.pojos.TermStatistics;
import com.ir.lightbringer.restclient.RestCallHandler;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Abhishek Mulay on 5/22/17.
 */
public class StatisticsProvider {
    private static RestCallHandler handler = new RestCallHandler();
    private static String INDEX_NAME = ConfigurationManager.getConfigurationValue("index.name");
    private static String TYPE_NAME = ConfigurationManager.getConfigurationValue("type.name");
    private final static String STATISTICS_API = "/" + INDEX_NAME + "/" + TYPE_NAME + "/_search?scroll=1m";
    private final static String scrollEndPoint = "_search/scroll";

    // avoid instantiation
    private StatisticsProvider() {}

    ///////////////////// Get statistics for all words in query ////////////////
    public static Map<String, List<TermStatistics>> getTermStatisticsForQuery(Query query) throws IOException {
        String cleanedQuery = query.getCleanedQuery();
        String[] terms = cleanedQuery.split(" ");

        Map<String, List<TermStatistics>> docIdTermStatisticsMap = new HashMap<>();

        for (String term : terms) {
            // get map of <docId, List[stats for terms in that docId]>
            Map<String, List<TermStatistics>> statistics = StatisticsProvider.getStatistics(term);

            // update main map with values
            for (Map.Entry<String, List<TermStatistics>> entry : statistics.entrySet()) {
                String documentId = entry.getKey();
                List<TermStatistics> statisticsForDocumentId = entry.getValue();

                if (docIdTermStatisticsMap.containsKey(documentId)) {
                    List<TermStatistics> previousTermStatistics = docIdTermStatisticsMap.get(documentId);
                    previousTermStatistics.addAll(statisticsForDocumentId);
                    docIdTermStatisticsMap.put(documentId, previousTermStatistics);
                } else {
                    docIdTermStatisticsMap.put(documentId, statisticsForDocumentId);
                }
            }
        }

        return docIdTermStatisticsMap;
    }

    // return map of <documentId, List<TermStatistics>>
    private static Map<String, List<TermStatistics>> getStatistics(String term) throws IOException {
        handler.openConnection();
        final String body = "{\n" +
                "    \"size\" : 4000,\n" +
                "    \"query\" : {\n" +
                "        \"term\": {\"text\": \"" + term + "\"}\n" +
                "    },\n" +
                "    \"_source\": \"docLength\", \n" +
                "    \"script_fields\" : {\n" +
                "        \"doc_frequency\" : {\n" +
                "            \"script\" : {\n" +
                "              \"lang\": \"groovy\",   \n" +
                "              \"inline\": \"_index['text']['" + term + "'].df()\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"term_frequency\" : {\n" +
                "            \"script\" : {\n" +
                "              \"lang\": \"groovy\",   \n" +
                "              \"inline\": \"_index['text']['" + term + "'].tf()\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"ttf\" : {\n" +
                "            \"script\" : {\n" +
                "              \"lang\": \"groovy\",   \n" +
                "              \"inline\": \"_index['text']['" + term + "'].ttf()\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        Response response = handler.get(body, STATISTICS_API);
        String jsonString = EntityUtils.toString(response.getEntity());
        handler.closeConnection();
        return extractStatistics(term, jsonString);
    }

    public static Map<String, List<TermStatistics>> getStatisticsForAllDocuments(String term) throws IOException {
        handler.openConnection();
        final String body = "{\n" +
                "    \"size\" : 10000,\n" +
                "    \"query\" : {\n" +
                "        \"match_all\": {}\n" +
                "    },\n" +
                "    \"_source\": \"docLength\", \n" +
                "    \"script_fields\" : {\n" +
                "        \"doc_frequency\" : {\n" +
                "            \"script\" : {\n" +
                "              \"lang\": \"groovy\",   \n" +
                "              \"inline\": \"_index['text']['" + term + "'].df()\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"term_frequency\" : {\n" +
                "            \"script\" : {\n" +
                "              \"lang\": \"groovy\",   \n" +
                "              \"inline\": \"_index['text']['" + term + "'].tf()\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"ttf\" : {\n" +
                "            \"script\" : {\n" +
                "              \"lang\": \"groovy\",   \n" +
                "              \"inline\": \"_index['text']['" + term + "'].ttf()\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        Response response = handler.get(body, STATISTICS_API);
        String jsonString = EntityUtils.toString(response.getEntity());
        handler.closeConnection();
        return extractStatistics(term, jsonString);
    }

    private static Map<String, List<TermStatistics>> extractStatistics(String term, String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        String scrollId = "";
        try {
            jsonNode = mapper.readTree(jsonString);
            if (jsonNode.has("_scroll_id")) {
                scrollId = jsonNode.get("_scroll_id").asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parsing tf, df and ttf.
        Map<String, List<TermStatistics>> docIdTermStatisticsMap = parseTermStatistics(jsonNode, term);

        handler.openConnection();
        if (!scrollId.isEmpty()) {
            boolean hitsPresent = true;
            while (hitsPresent) {
                final String scrollBody = "{\n" +
                        "    \"scroll\" : \"1m\", \n" +
                        "    \"scroll_id\": \"" + scrollId + "\"\n" +
                        "}\n";

                Response nextPageResponse = handler.post(scrollBody, scrollEndPoint);
                String nextJsonString = null;
                JsonNode nextPageJsonNode = null;
                try {
                    nextJsonString = EntityUtils.toString(nextPageResponse.getEntity());
                    nextPageJsonNode = mapper.readTree(nextJsonString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                hitsPresent = nextPageJsonNode.get("hits").get("hits").size() > 0;
                if (jsonNode.has("_scroll_id")) {
                    scrollId = nextPageJsonNode.get("_scroll_id").asText();
                }
                // parsing here again
                Map<String, List<TermStatistics>> docIdTermStatsMapFromNextPage = parseTermStatistics(nextPageJsonNode, term);
                docIdTermStatisticsMap.putAll(docIdTermStatsMapFromNextPage);
            }
        }
        System.out.println("Total term statistics for '" + term + "' = " + docIdTermStatisticsMap.size());
        handler.closeConnection();
        return docIdTermStatisticsMap;
    }

    private static Map<String, List<TermStatistics>> parseTermStatistics(JsonNode jsonNode, String term) {
        // return map of <documentId, List<TermStatistics>>
        Map<String, List<TermStatistics>> termStatisticsMap = new HashMap<>();

        if (jsonNode.has("hits")) {
            boolean hitsPresent = jsonNode.get("hits").get("hits").size() > 0;
            if (hitsPresent) {
                JsonNode hitsArray = jsonNode.get("hits").get("hits");
                for (final JsonNode hit : hitsArray) {
                    String documentId = hit.has("_id") ? hit.get("_id").asText() : "";
                    int docLength = hit.has("_source") ? hit.get("_source").get("docLength").asInt() : -1;
                    if (hit.has("fields")) {
                        int tf = hit.get("fields").has("term_frequency") ? hit.get("fields").get("term_frequency").get(0).asInt() : -1;
                        int df = hit.get("fields").has("doc_frequency") ? hit.get("fields").get("doc_frequency").get(0).asInt() : -1;
                        int ttf = hit.get("fields").has("ttf") ? hit.get("fields").get("ttf").get(0).asInt() : -1;

                        TermStatistics termStatistics = new TermStatistics(term, documentId, docLength, tf, df, ttf);
                        if (termStatisticsMap.containsKey(documentId)) {
                            List<TermStatistics> stats = termStatisticsMap.get(documentId);
                            stats.add(termStatistics);
                            termStatisticsMap.put(documentId, stats);
                        } else {
                            List<TermStatistics> newList = new ArrayList<>();
                            newList.add(termStatistics);
                            termStatisticsMap.put(documentId, newList);
                        }
                    }
                }
            }
        }
        return termStatisticsMap;
    }
}

