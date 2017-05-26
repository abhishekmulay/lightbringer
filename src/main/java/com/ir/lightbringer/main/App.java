package com.ir.lightbringer.main;

import com.ir.lightbringer.indexing.datareader.DataReader;
import com.ir.lightbringer.indexing.datawriter.DataWriter;
import com.ir.lightbringer.pojos.HW1Model;
import com.ir.lightbringer.queryprocessor.FileQueryReader;
import com.ir.lightbringer.pojos.Query;
import com.ir.lightbringer.queryprocessor.QueryProcessor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class App {

    public static void startIndexing() {
        DataWriter writer = new DataWriter();
        DataReader reader = new DataReader();

        String DATA_PATH = ConfigurationManager.getConfigurationValue("data.set.path");
        List<File> dataFiles = reader.getAllDataFiles(DATA_PATH);

        long timeAtStart = System.nanoTime();
        for (File f : dataFiles) {
            List<HW1Model> hw1Models = null;
            try {
                hw1Models = reader.readFileIntoModel(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Map<String, String> modelsToJSON = reader.convertModelsToJSON(hw1Models);
            writer.bulkInsertDocuments(modelsToJSON);
        }
        long timeAtEnd = System.nanoTime();

        long elapsedTime = timeAtEnd - timeAtStart;
        double seconds = (double) elapsedTime / 1000000000.0;
        System.out.println("Total time taken: " + seconds / 60.0 + " minutes");
    }

    public static void runVectorSpaceModels() {

        String okapiOutputFile = ConfigurationManager.getConfigurationValue("okapi.output.file");
        FileQueryReader reader = new FileQueryReader();
        List<Query> allQueries = reader.getAllQueries();

        QueryProcessor processor = new QueryProcessor();

    }

    public static void main(String[] args) {
        // Writes output to output.txt file instead of stdout
        try {
            System.setOut(new PrintStream(new File("output.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        long timeAtStart = System.nanoTime();

//        runVectorSpaceModels();
        startIndexing();

        long timeAtEnd = System.nanoTime();
        long elapsedTime = timeAtEnd - timeAtStart;
        double seconds = (double) elapsedTime / 1000000000.0;
        System.out.println("Total time taken: " + seconds / 60.0 + " minutes");
    }
}