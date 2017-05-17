package com.ir.lightbringer.datareader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.models.HW1Model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

/**
 * Created by Abhishek Mulay on 5/10/17.
 */
public class DataReader {
    final String DATA_PATH = ConfigurationManager.getConfigurationValue("data.set.path");
    final String TEST_DATA_PATH = ConfigurationManager.getConfigurationValue("test.data.set.path");

    public File[] getAllDataFiles(String PATH) {
        File folder = new File(PATH);
        return folder.listFiles();
    }

    public List<HW1Model> readFileIntoModel(File dataFile) throws IOException {
        List<HW1Model> models = new ArrayList<HW1Model>();
        String charset = "UTF-8";

        InputStream content = new FileInputStream(dataFile);
        Document document = Jsoup.parse(content, charset, "", Parser.xmlParser());
        content.close();
        Elements docTags = document.getElementsByTag("DOC");
        for (Element docTag : docTags) {
            // primary fields
            String docId = docTag.select("DOCNO").text();
            String text = docTag.select("TEXT").text();

            // extra fields
            String fileId = docTag.select("FILEID").text();
            String first = docTag.select("FIRST").text();
            String second = docTag.select("SECOND").text();
            String dateline = docTag.select("DATELINE").text();
            List<String>  heads = new ArrayList<String>();
            for (Element head : docTag.select("HEAD")) {
                heads.add(head.text());
            }
            List<String>  bylines = new ArrayList<String>();
            for (Element byline : docTag.select("BYLINE")) {
                bylines.add(byline.text());
            }

            models.add(new HW1Model(docId, text, fileId, first, second, dateline, heads, bylines));
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

    public Map<String, String> convertModelsToJSON(List<HW1Model> hw1Models) {
        Map<String, String> jsons = new HashMap<String, String>();
        for (HW1Model model : hw1Models) {
            jsons.put(model.getDocumentId(), convertModelToJSON(model));
        }
        return jsons;
    }

    public Map<String, String> getDataSetAsJSON() {
        try {
            return getJSONDataSet("JSON");
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Could not read test data into JSON");
    }

    public Map<String, String> getTestDatasetAsJSON() {
        try {
            return getJSONDataSet("TEST");
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Could not read data into JSON");
    }

    private Map<String, String> getJSONDataSet(String dataSetType) throws IOException {
//        List<String> jsonData =  new ArrayList<String>();
        Map<String, String> jsonData = new HashMap<String, String>();
        DataReader dsReader = new DataReader();
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
