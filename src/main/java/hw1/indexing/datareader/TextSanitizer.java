package hw1.indexing.datareader;


import hw1.main.ConfigurationManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Abhishek Mulay on 6/1/17.
 */


//https://stackoverflow.com/questions/18830813/how-can-i-remove-punctuation-from-input-text-in-java

public class TextSanitizer {

    private static final String STOP_WORDS_FILE_PATH = ConfigurationManager.getConfigurationValue("stop.words.file.path");

    // remove special characters like , etc. split into words and remove whitespace
    public static String[] tokenize(String text) {
        return text.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
    }


    public static String removeStopWords (String text) {
        List<String> stopWords = Collections.unmodifiableList(getStopWords());
        String[] tokens = tokenize(text);
        List<String> tokenList = new ArrayList<>(Arrays.asList(tokens));
        tokenList.removeAll(stopWords);

        StringBuffer buffer = new StringBuffer();
        for (String token : tokenList)
            buffer.append(token);

        return buffer.toString();
    }


    private static List<String> getStopWords() {
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
