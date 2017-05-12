package com.ir.lightbringer.main;

import com.ir.lightbringer.datareader.DataReader;
import com.ir.lightbringer.datawriter.DataWriter;
import com.ir.lightbringer.models.HW1Model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) throws IOException {
        DataWriter writer = new DataWriter();
        DataReader reader = new DataReader();

        String DATA_PATH = ConfigurationManager.getConfigurationValue("data.set.path");
        File[] dataFiles = reader.getAllDataFiles(DATA_PATH);

        for (File f : dataFiles) {
            List<HW1Model> hw1Models = reader.readFileIntoModel(f);
            Map<String, String> modelsToJSON = reader.convertModelsToJSON(hw1Models);
            writer.bulkInsertDocuments(modelsToJSON);
        }


    }
}