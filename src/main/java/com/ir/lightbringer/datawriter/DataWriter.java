package com.ir.lightbringer.datawriter;

import com.ir.lightbringer.datareader.DataReader;
import com.ir.lightbringer.main.ConfigurationManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.*;

/**
 * Created by Abhishek Mulay on 5/11/17.
 */
public class DataWriter {

    private String INDEX_NAME = ConfigurationManager.getConfigurationValue("index.name");
    private String TYPE_NAME = ConfigurationManager.getConfigurationValue("type.name");
    final static int CHUNK_SIZE = Integer.parseInt(ConfigurationManager.getConfigurationValue("chunk.size"));

    private HttpHost localHost;
    private  RestClient restClient = null;

    public DataWriter() {
        this.localHost = new HttpHost("localhost", 9200, "http");
    }

    //    { "index" : { "_index" : "main", "_type" : "hw1", "_id" : "ABC123" } }
    //    { "field1" : "value1" }
    public void insertChunks(Map<String, String> jsonMap) {
        DataReader reader = new DataReader();
        String actionMetaData = "";
        String json = "";
        String documentId = "";
        StringBuffer bulkRequestBody = new StringBuffer();

        for (Map.Entry<String, String> entry : jsonMap.entrySet()) {
            documentId = entry.getKey();
            json = entry.getValue();
            actionMetaData = String.format
                    ("{ \"index\" : { \"_index\" : \"%s\", \"_type\" : \"%s\", \"_id\" : \"%s\" } }%n",
                            INDEX_NAME, TYPE_NAME, documentId);
            bulkRequestBody.append(actionMetaData);
            bulkRequestBody.append(json);
            bulkRequestBody.append("\n");
        }
        System.out.println("Bulk inserting " + jsonMap.size() + " documents in " + INDEX_NAME);
        HttpEntity entity = new NStringEntity(bulkRequestBody.toString(), ContentType.APPLICATION_JSON);
        String BULK_API_ENDPOINT = '/' + INDEX_NAME + '/' + INDEX_NAME + "/_bulk";
        try {
            Response response = restClient.performRequest
                    ("POST", BULK_API_ENDPOINT, Collections.<String, String>emptyMap(), entity);
            System.out.println("STATUS: " + response.getStatusLine().getStatusCode());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, String>> createSubMaps(Map<String, String> jsonMap) {
        List<Map<String,String>> mapList = new ArrayList<Map<String, String>>();
        int count=0;
        Map<String, String> subMap = new HashMap<String, String>();
        for (Map.Entry<String, String> entry :jsonMap.entrySet()) {
            String documentId = entry.getKey();
            String json = entry.getValue();
            subMap.put(documentId, json);
            if (count % CHUNK_SIZE == 0) {
                mapList.add(subMap);
                subMap = new HashMap<String, String>();
            }
            count = count + 1;
        }
        // elements less than chunk size remaining in last map
        mapList.add(subMap);
        return mapList;
    }

    public void bulkInsertDocuments(Map<String, String> allJsonMap) {
        this.restClient = RestClient.builder(this.localHost).build();
        // Divide the big map of JSON strings into small maps each containing 1000 json.
        if (allJsonMap.size() > CHUNK_SIZE) {
            List<Map<String, String>> jsonMaps = createSubMaps(allJsonMap);
            for (Map<String, String> jsonMap : jsonMaps) {
                // insert 1000 json documents at once.
                insertChunks(jsonMap);
            }
        } else {
            insertChunks(allJsonMap);
        }
        try {
            this.restClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
