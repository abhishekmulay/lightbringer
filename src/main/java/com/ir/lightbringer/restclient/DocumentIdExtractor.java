package com.ir.lightbringer.restclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek Mulay on 5/19/17.
 */
public class DocumentIdExtractor {

    private RestCallHandler handler = new RestCallHandler();

    private List<String> getDocumentIdsFromResponse(final Response response) {
        HttpEntity entity = response.getEntity();
        String jsonResponse = null;
        JsonNode jsonNode = null;

        try {
            jsonResponse = EntityUtils.toString(entity);
            System.out.println("Getting docIds from: \n" + jsonResponse);
            ObjectMapper mapper = new ObjectMapper();
            jsonNode = mapper.readTree(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractDoumentIds(jsonNode);
    }

    private List<String> extractDoumentIds(JsonNode jsonNode) {
        JsonNode hitsArray = jsonNode.get("hits").get("hits");
        List<String> docids = new ArrayList<String>();
        if (hitsArray.isArray()) {
//            hit =>
//            {"_index":"ap_dataset","_type":"hw1","_id":"AP890101-0060","_score":1.0,"fields":{"docno":["AP890101-0060"]}}
            for (JsonNode hit : hitsArray) {
                if (hit.has("fields") && hit.get("fields").has("docno")) {
                    String docno = hit.get("fields").get("docno").get(0).asText();
                    docids.add(docno);
                }
            }
        }
        return docids;
    }

    public Response fetchFirstPage() {
        final int batchSize = 10000;
        final String body = "{\n" +
                "    \"size\": "+batchSize+",\n" +
                "    \"stored_fields\": [ \"docno\"]\n" +
                "}";
        final String endPoint = "/ap_dataset/hw1/_search?scroll=1m";

        this.handler.initializeConnection();
        Response response = handler.post(body, endPoint);
        this.handler.closeConnection();
        return response;
    }

    public String getScrollId (Response response) {
        String scrollId = "";
        try {
            HttpEntity entity = response.getEntity();
            String jsonString = EntityUtils.toString(entity);
            System.out.println("Finding scrollId in json: \n" + jsonString);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonString);
            scrollId = jsonNode.has("_scroll_id") ? jsonNode.get("_scroll_id").asText() : "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scrollId;
    }

    private Response fetchNextPage(final String scrollId) {
        final String SCROLL_API = "/_search/scroll";
        return fetchNextScrollPage(scrollId, SCROLL_API);
    }

    //    POST  /_search/scroll
    //    {
    //        "scroll" : "1m",
    //        "scroll_id": "DXF1ZXJ5QW5kRmV0Y2gBAAAAAAAAAYYWT1B1dnFXVjVTM09sc0hHLXo5QTltQQ=="
    //    }
    //
    private Response fetchNextScrollPage(final String scrollId, final String endPoint) {
        String aliveTime = "1m";
        String requestBody = "{\n" +
                "    \"scroll\" : \" "+ aliveTime +"\", \n" +
                "    \"scroll_id\": " + scrollId +
                "}\n";

        this.handler.closeConnection();
        Response response = handler.post(requestBody, endPoint);
        this.handler.closeConnection();
        return response;
    }

    public List<String> fetchAllDocIds() {
        this.handler.initializeConnection();
        List<String> documentIds = new ArrayList<String>();
        final Response response = fetchFirstPage();

        String scrollId = getScrollId(response);

        List<String> documentIdsFromResponse = getDocumentIdsFromResponse(response);
        documentIds.addAll(documentIdsFromResponse);

        //noinspection Since15
        while(!scrollId.isEmpty()) {
            System.out.println("ScrollId = " + scrollId);
            Response nextPageResponse = fetchNextPage(scrollId);
            List<String> docIds = getDocumentIdsFromResponse(nextPageResponse);
            documentIds.addAll(docIds);
            scrollId = getScrollId(nextPageResponse);
        }

        this.handler.closeConnection();
        return documentIds;
    }

//    GET /ap_dataset/hw1/_search?scroll=1m
//    {
//        "stored_fields" : ["docno"],
//        "query" : {
//              "match_all" : {}
//         }
//    }
    public void sushantQuery () throws IOException {
        final String endPoint = "/ap_dataset/hw1/_search?scroll=1m";
        final String body = "    {\n" +
                            "        \"stored_fields\" : [\"docno\"],\n" +
                            "        \"query\" : {\n" +
                            "              \"match_all\" : {}\n" +
                            "         }\n" +
                            "    }\n";
        this.handler.initializeConnection();
        Response response = this.handler.get(body, endPoint);
        String jsonString = EntityUtils.toString(response.getEntity());
        System.out.println(jsonString);
    }

    public static void main(String[] args) throws IOException {
        try {
            System.setOut(new PrintStream(new File("output-file.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        DocumentIdExtractor extractor = new DocumentIdExtractor();
        extractor.sushantQuery();

//        Response response = extractor.fetchFirstPage();
//        String scrollId = extractor.getScrollId(response);
//
//        String jsonString = EntityUtils.toString(response.getEntity());
//        System.out.println(jsonString);
//
//        System.out.println("scrollId = "+ scrollId);

//        List<String> docIds = extractor.fetchAllDocIds();
//        System.out.println("fetched docIds = " + docIds.size());
    }

}
