package com.ir.lightbringer.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Abhishek Mulay on 5/11/17.
 */
public class ConfigurationManager {
    private static Properties properties = new Properties();
    private static InputStream  stream = null;
    final static String PROPERTIES_FILE_PATH = "";

    static {
        try {
            stream = new FileInputStream("src/main/resources/config.properties");
            properties.load(stream);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static String getConfigurationValue(String propertyName) {
        return properties.getProperty(propertyName);
    }
}
