package com.ir.lightbringer.restclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.lightbringer.main.ConfigurationManager;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Abhishek Mulay on 5/19/17.
 */
public class DocumentIdExtractor {

    private RestCallHandler handler = new RestCallHandler();
    private String INDEX_NAME = ConfigurationManager.getConfigurationValue("index.name");
    private String TYPE_NAME = ConfigurationManager.getConfigurationValue("type.name");
    private final String keepAliveTime = "2m";
    private String endPoint = "/"+INDEX_NAME+"/" + TYPE_NAME + "/_search?scroll=" + keepAliveTime;
    private String scrollEndPoint = "_search/scroll";

    @SuppressWarnings("Since15")
    public Set<String> getAllDocumentIds() throws IOException {
        handler.openConnection();
        final String body = "";
        Set<String> docids = new HashSet<String>();
        String scrollId = "";

            Response response = this.handler.get(body, endPoint);
            HttpEntity entity = response.getEntity();
            String jsonString = EntityUtils.toString(entity);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonString);

            if (jsonNode.has("_scroll_id")) {
                scrollId = jsonNode.get("_scroll_id").asText();
                docids.addAll(extraceDocumentIds(jsonNode));
            }

        boolean hitsPresent = true;
        while (hitsPresent) {
            final String scrollBody = "{\n" +
                    "    \"scroll\" : \"1m\", \n" +
                    "    \"scroll_id\": \""+scrollId+"\"\n" +
                    "}\n";

            Response nextPageResponse = handler.post(scrollBody, scrollEndPoint);
            String nextJson = EntityUtils.toString(nextPageResponse.getEntity());
            JsonNode jsonTree = mapper.readTree(nextJson);
            hitsPresent = jsonTree.get("hits").get("hits").size() > 0;
            if (jsonNode.has("_scroll_id")) {
                scrollId = jsonTree.get("_scroll_id").asText();
            }
            docids.addAll(extraceDocumentIds(jsonTree));
        }
        handler.closeConnection();
        return docids;
    }

    private Set<String> extraceDocumentIds(JsonNode jsonNode) {
        Set<String> docids = new HashSet<String>();
        JsonNode hitsArray = jsonNode.get("hits").get("hits");
        if (hitsArray.isArray()) {
            // hit => {"_index":"ap_dataset","_type":"hw1","_id":"AP890101-0060","_score":1.0,"fields":{"docno":["AP890101-0060"]}}
            for (JsonNode hit : hitsArray) {
                if (hit.has("_source") && hit.get("_source").has("docno")) {
                    String docno = hit.get("_source").get("docno").asText();
                    docids.add(docno);
                }
            }
        }
        return docids;
    }
}