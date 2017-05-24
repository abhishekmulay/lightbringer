package com.ir.lightbringer.statistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.restclient.RestCallHandler;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;

import java.io.IOException;
import java.util.HashMap;
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

    public static Map<String, TermStatistics> getStatistics(String term) throws IOException {
        handler.openConnection();
        final String body = "{\n" +
                "    \"size\" : 1000,\n" +
                "    \"query\" : {\n" +
                "        \"term\": {\"text\": \"" + term + "\"}\n" +
                "    },\n" +
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

    private static Map<String, TermStatistics> extractStatistics(String term, String jsonString) {
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
        Map<String, TermStatistics> termStatisticsMap = parseTermStatistics(jsonNode, term);

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
                Map<String, TermStatistics> stringTermStatisticsMapFromNextPage = parseTermStatistics(nextPageJsonNode, term);
                termStatisticsMap.putAll(stringTermStatisticsMapFromNextPage);
            }
        }
        System.out.println("Total term statistics " + termStatisticsMap.size());
        handler.closeConnection();
        return termStatisticsMap;
    }

    private static Map<String, TermStatistics> parseTermStatistics(JsonNode jsonNode, String term) {
        Map<String, TermStatistics> termStatisticsMap = new HashMap<String, TermStatistics>();
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
                        termStatisticsMap.put(documentId, termStatistics);
                    }
                }
            }
        }
        return termStatisticsMap;
    }
}

