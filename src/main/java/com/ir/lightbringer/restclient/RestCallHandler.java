package com.ir.lightbringer.restclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.models.HW1Model;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by Abhishek Mulay on 5/17/17.
 */
public class RestCallHandler {

    private final String INDEX_NAME = ConfigurationManager.getConfigurationValue("index.name");
    private final String TYPE_NAME = ConfigurationManager.getConfigurationValue("type.name");
    private final String BULK_API_ENDPOINT = '/' + INDEX_NAME + '/' + INDEX_NAME + "/_bulk";
    private final String DOCUMENT_API = '/' + INDEX_NAME + '/' + TYPE_NAME + '/';

    private HttpHost localHost = null;
    private RestClient restClient = null;

    public void initializeConnection() {
        this.localHost = new HttpHost("localhost", 9200, "http");
        this.restClient = RestClient.builder(this.localHost).build();
    }

    public void closeConnection() {
        try {
            this.restClient.close();
            this.restClient = null;
            this.localHost = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Response bulkPOST(String bulkRequestBody) {
        HttpEntity entity = new NStringEntity(bulkRequestBody, ContentType.APPLICATION_JSON);
        Response response = null;
        try {
            response = restClient.performRequest("POST", BULK_API_ENDPOINT, Collections.<String, String>emptyMap(), entity);
            System.out.println("STATUS: " + response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public Response get(String documentId) {
        HttpEntity entity = new NStringEntity(DOCUMENT_API + documentId, ContentType.APPLICATION_JSON);
        Response response = null;
        try {
            response = restClient.performRequest("GET", DOCUMENT_API + documentId, Collections.singletonMap("pretty", "true"),
                    entity);
            System.out.println("STATUS: " + response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }


    public static void main(String[] args) {
        RestCallHandler handler = new RestCallHandler();
        handler.initializeConnection();
        Response response = handler.get("AP890101-0068");

        try {
            HttpEntity entity = response.getEntity();
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = EntityUtils.toString(entity);
            System.out.println("jsonString: \n" + jsonString);
//            HW1Model model = objectMapper.readValue(jsonString, HW1Model.class);
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            System.out.println(jsonNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("response " + response.toString());
        handler.closeConnection();
    }

}
