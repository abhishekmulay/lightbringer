package hw1.indexing.datareader;

import com.fasterxml.jackson.core.JsonProcessingException;
import hw1.main.ConfigurationManager;
import hw1.pojos.HW1Model;
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

    public ArrayList<File> getAllDataFiles(String PATH) {
        File folder = new File(PATH);
        File[] allFiles = folder.listFiles();
        // exlucde README file
        ArrayList<File> files = new ArrayList<File>(Arrays.asList(allFiles));
        File toReomve = null;
        for (File f : files) {
            if (f.getName().equalsIgnoreCase("readme")) {
                toReomve = f;
            }
        }
        files.remove(toReomve);
        return files;
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
            List<String> heads = new ArrayList<String>();
            for (Element head : docTag.select("HEAD")) {
                heads.add(head.text());
            }
            List<String> bylines = new ArrayList<String>();
            for (Element byline : docTag.select("BYLINE")) {
                bylines.add(byline.text());
            }

            String[] tokens = TextSanitizer.tokenize(text);
            String cleanedText = String.join(" ", tokens);
            models.add(new HW1Model(docId, cleanedText, fileId, first, second, dateline, heads, bylines));
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
            jsons.put(model.getDocno(), convertModelToJSON(model));
        }
        return jsons;
    }

    public Map<String, String> getDataSetAsJSON() {
        try {
            return getJSONDataSet("JSON");
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Could not getCatalogAsMap test data into JSON");
    }

    public Map<String, String> getTestDatasetAsJSON() {
        try {
            return getJSONDataSet("TEST");
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Could not getCatalogAsMap data into JSON");
    }

    private Map<String, String> getJSONDataSet(String dataSetType) throws IOException {
//        List<String> jsonData =  new ArrayList<String>();
        Map<String, String> jsonData = new HashMap<String, String>();
        DataReader dsReader = new DataReader();
        List<File> dataFiles = null;
        if (dataSetType.equals("TEST")) {
            dataFiles = dsReader.getAllDataFiles(TEST_DATA_PATH);
        } else {
            dataFiles = dsReader.getAllDataFiles(DATA_PATH);
        }

        for (File dataFile : dataFiles) {
            List<HW1Model> models = dsReader.readFileIntoModel(dataFile);
            for (HW1Model model : models) {
                String json = dsReader.convertModelToJSON(model);
                jsonData.put(model.getDocno(), json);
//                jsonData.add(json);
            }
        }
        System.out.println(jsonData.size() + " json objects created.");
        return jsonData;
    }
}
