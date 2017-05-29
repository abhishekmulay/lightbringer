package com.ir.lightbringer.queryprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.restclient.RestCallHandler;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek Mulay on 5/17/17.
 */
public class QueryCleaner {

    private static RestCallHandler handler = new RestCallHandler();
    private static String INDEX_NAME = ConfigurationManager.getConfigurationValue("index.name");
    private static final String STOP_WORDS_FILE_PATH = ConfigurationManager.getConfigurationValue("stop.words.file.path");
    private final static String ANALYSER_ENDPOINT = "/" + INDEX_NAME + "/" + "_analyze?filter_path=tokens.token";


    public static String analyzeString(String query) {

        final String body = "{\n" +
                "  \"analyzer\": \"my_english\",\n" +
                "  \"text\": \"" + query + "\" \n" +
                "}";
        handler.openConnection();
        Response response = handler.post(body, ANALYSER_ENDPOINT);
        handler.closeConnection();

        StringBuffer buffer = new StringBuffer();
        try {
            String jsonString = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonString);
            boolean hasTokens = jsonNode.has("tokens") && jsonNode.get("tokens").size() > 0;
            if (hasTokens) {
                for (JsonNode token : jsonNode.get("tokens")) {
                    String termToken = token.get("token").asText();
                    buffer.append(termToken).append(" ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public static List<String> getStopWords() {
        List<String> stopWords = new ArrayList<String>();
        try {
            InputStream content = new FileInputStream(STOP_WORDS_FILE_PATH);
            BufferedReader br = new BufferedReader(new InputStreamReader(content));
            while (br.readLine() != null) {
                stopWords.add(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopWords;
    }

}
