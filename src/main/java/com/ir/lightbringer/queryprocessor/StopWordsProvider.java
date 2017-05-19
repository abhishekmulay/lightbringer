package com.ir.lightbringer.queryprocessor;

import com.ir.lightbringer.main.ConfigurationManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek Mulay on 5/17/17.
 */
public class StopWordsProvider {
    private static final String STOP_WORDS_FILE_PATH = ConfigurationManager.getConfigurationValue("stop.words.file.path");

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
