package com.ir.datawriter;

import com.ir.datareader.DatasetReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
//import static org.elasticsearch.common.xcontent.XContentFactory.*;

import java.util.*;

/**
 * Created by Abhishek Mulay on 5/11/17.
 */
public class DataWriter {

    private RestClient restClient;
    String INDEX_NAME = "lightbringer";
    String TYPE_NAME = "hw1";

    final static int CHUNK_SIZE = 1000;


    public DataWriter() {
        HttpHost localHost = new HttpHost("localhost", 9200, "http");
        this.restClient = RestClient.builder(localHost).build();
    }

    public void insertChunks(Map<String, String> jsonMap) {
        DatasetReader reader = new DatasetReader();
        String index = "lightbringer";
        String type = "hw1";
        String actionMetaData = "";
        String json = "";
        String documentId = "";
        StringBuilder bulkRequestBody = new StringBuilder();

        for (Map.Entry<String, String> entry : jsonMap.entrySet()) {
            documentId = entry.getKey();
            json = entry.getValue();
            actionMetaData = String.format
                    ("{ \"index\" : { \"_index\" : \"%s\", \"_type\" : \"%s\", \"_id\" : \"%s\" } }%n",
                            index, type, documentId);
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

    public void bulkWriteDocuments(Map<String, String> allJsonMap) {
        // Divide the big map of JSON strings into small maps each containing 1000 json.
        List<Map<String, String>> jsonMaps = createSubMaps(allJsonMap);
        for (Map<String, String> jsonMap : jsonMaps) {
            // insert 1000 json documents at once.
            insertChunks(jsonMap);
        }
    }

    public static void main(String[] args) {
        DataWriter writer = new DataWriter();
        DatasetReader reader = new DatasetReader();
        writer.bulkWriteDocuments(reader.getDataSetAsJSON());
    }
}
