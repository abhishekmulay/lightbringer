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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {

    ////////////////  INDEXING  /////////////////////////////
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

    ///////////  VECTOR SPACE MODELS ////////////////////////
    public static void runOkapi() {
        String okapiOutputFile = ConfigurationManager.getConfigurationValue("okapi.output.file");
        File file = new File(okapiOutputFile);
        file.delete(); // delete previous file
        FileQueryReader reader = new FileQueryReader();
        List<Query> allQueries = reader.getAllQueries();
        QueryProcessor processor = new QueryProcessor();
        processor.calculateOkapi_tf(allQueries, okapiOutputFile);
    }

    public static void runTfIdf() {
        String tfIdfOutputFile = ConfigurationManager.getConfigurationValue("tfidf.output.file");
        File file = new File(tfIdfOutputFile);
        file.delete(); // delete previous file
        FileQueryReader reader = new FileQueryReader();
        List<Query> allQueries = reader.getAllQueries();
        QueryProcessor processor = new QueryProcessor();
        processor.calculateTfIdf(allQueries, tfIdfOutputFile);
    }

    public static void runBM25() {
        String bm25OutputFile = ConfigurationManager.getConfigurationValue("bm-25.output.file");
        File file = new File(bm25OutputFile);
        file.delete(); // delete previous file
        FileQueryReader reader = new FileQueryReader();
        List<Query> allQueries = reader.getAllQueries();
        QueryProcessor processor = new QueryProcessor();
        processor.calculateOkapiBM25(allQueries, bm25OutputFile);
    }

    ///////////  LANGUAGE MODELS ///////////////////////////
    public static void runUnigramWithLaplaceSmoothing() {
        String unigramWithLaplaceSmoothingOutputFile = ConfigurationManager.getConfigurationValue("laplace.output.file");
        File file = new File(unigramWithLaplaceSmoothingOutputFile);
        file.delete(); // delete previous file
        FileQueryReader reader = new FileQueryReader();
        List<Query> allQueries = reader.getAllQueries();
        QueryProcessor processor = new QueryProcessor();
        processor.calculateUnigramWithLaplaceSmoothing(allQueries, unigramWithLaplaceSmoothingOutputFile);
    }

    public static void runUnigramWithJelinekMercerSmoothing() {
        String unigramWithJelinekSmoothingOutputFile = ConfigurationManager.getConfigurationValue("jelinek.output.file");
        File file = new File(unigramWithJelinekSmoothingOutputFile);
        file.delete(); // delete previous file
        FileQueryReader reader = new FileQueryReader();
        List<Query> allQueries = reader.getAllQueries();
        QueryProcessor processor = new QueryProcessor();
        processor.calculateUnigramWithJeliekSmoothing(allQueries, unigramWithJelinekSmoothingOutputFile);
    }

    //////////////////// MAIN /////////////////////////////
    public static void main(String[] args) {
        // Writes output to output.txt file instead of stdout
        try {
            System.setOut(new PrintStream(new File("output.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        long timeAtStart = System.nanoTime();

//       For indexing documents
//        startIndexing();

//        runOkapi();
//        runTfIdf();
//        runBM25();
//        runUnigramWithLaplaceSmoothing();
        runUnigramWithJelinekMercerSmoothing();

        long timeAtEnd = System.nanoTime();
        long elapsedTime = timeAtEnd - timeAtStart;
        double seconds = (double) elapsedTime / 1000000000.0;
        System.out.println("Total time taken: " + seconds / 60.0 + " minutes");
    }
}