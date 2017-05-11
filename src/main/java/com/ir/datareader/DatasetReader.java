package com.ir.datareader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ir.models.HW1Model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

/**
 * Created by Abhishek Mulay on 5/10/17.
 */
public class DatasetReader {
    String DATA_PATH = "/Users/abhishek/Google Drive/NEU/summer-17/IR/IR_data/AP_DATA/ap89_collection";
    String TEST_DATA_PATH = "/Users/abhishek/Google Drive/NEU/summer-17/IR/IR_data/AP_DATA/testing_ap89_collection";

    private File[] getAllDataFiles(String PATH) {
        File folder = new File(PATH);
        return folder.listFiles();
    }

    public List<HW1Model> readFileIntoModel(File dataFile) {
        List<HW1Model> models = new ArrayList<HW1Model>();
        String charset = "UTF-8";
        List<String> heads = new ArrayList<String>();
        List<String> bylines = new ArrayList<String>();
        try {
            Document document = Jsoup.parse(dataFile, charset);
            Elements docTags = document.getElementsByTag("DOC");
            for (Element docTag : docTags) {
                String docId = docTag.select("DOCNO").text();
                String text = docTag.select("TEXT").text();

                String fileId = docTag.select("FILEID").text();
                String first = docTag.select("FIRST").text();
                String second = docTag.select("SECOND").text();
                String dateline = docTag.select("DATELINE").text();
                for (Element head : docTag.getElementsByTag("HEAD")) {
                    heads.add(head.text());
                }
                for (Element byline : docTag.select("BYLINE")) {
                    bylines.add(byline.text());
                }

                models.add(new HW1Model(docId, text, fileId, first, second, dateline, heads, bylines));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return models;
    }

    public String convertModelToJSON(HW1Model model) {
//        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        ObjectWriter writer = new ObjectMapper().writer();
        String json = "";
        try {
            json = writer.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public Map<String, String> getDataSetAsJSON() {
        return getJSONDataSet("JSON");
    }

    public Map<String, String> getTestDatasetAsJSON() {
        return getJSONDataSet("TEST");
    }

    private Map<String, String> getJSONDataSet(String dataSetType) {
//        List<String> jsonData =  new ArrayList<String>();
        Map<String, String> jsonData = new HashMap<String, String>();
        DatasetReader dsReader = new DatasetReader();
        File[] dataFiles = null;
        if (dataSetType.equals("TEST")) {
            dataFiles = dsReader.getAllDataFiles(TEST_DATA_PATH);
        } else {
            dataFiles = dsReader.getAllDataFiles(DATA_PATH);
        }

        for (File dataFile : dataFiles) {
            List<HW1Model> models = dsReader.readFileIntoModel(dataFile);
            for (HW1Model model : models) {
                String json = dsReader.convertModelToJSON(model);
                jsonData.put(model.getDocumentId(), json);
//                jsonData.add(json);
            }
        }
        System.out.println(jsonData.size() + " json objects created.");
        return jsonData;
    }
}
