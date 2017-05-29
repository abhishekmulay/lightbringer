package com.ir.lightbringer.queryprocessor;

import com.ir.lightbringer.main.ConfigurationManager;
import com.ir.lightbringer.pojos.Query;
import com.ir.lightbringer.ranking.languagemodels.UnigramWithJelinekSmoothingCalculator;
import com.ir.lightbringer.ranking.vectorspacemodels.BM25Calculator;
import com.ir.lightbringer.ranking.languagemodels.UnigramWithLaplaceSmoothingCalculator;
import com.ir.lightbringer.pojos.VectorStatistics;
import com.ir.lightbringer.restclient.DocumentIdExtractor;
import com.ir.lightbringer.util.MapUtils;
import com.ir.lightbringer.ranking.vectorspacemodels.OkapiTFCalculator;
import com.ir.lightbringer.statistics.StatisticsProvider;
import com.ir.lightbringer.pojos.TermStatistics;
import com.ir.lightbringer.ranking.vectorspacemodels.TfIdfCalculator;

import java.io.IOException;
import java.util.*;

/**
 * Created by Abhishek Mulay on 5/20/17.
 */
public class QueryProcessor {

    public void calculateOkapi_tf(List<Query> queryList, String okapiOutputFile) {
        for (Query query : queryList) {
            System.out.println("\n\nCalculating Okapi for: " + query.getCleanedQuery());

            // <docId, [TermStatistics]>
            Map<String, List<TermStatistics>> termStatistics = null;
            try {
//                termStatistics = StatisticsProvider.getTermStatisticsForQuery(query);
//                Map<String, Double> docIdOkapiValuesMap = OkapiTFCalculator.okapi_tf(termStatistics);
                Map<String, List<VectorStatistics>> vectorTermStatisticsForQuery = StatisticsProvider.getVectorTermStatisticsForQuery(query);
                Map<String, Double> docIdOkapiValuesMap = OkapiTFCalculator.okapi_tf_from_es(vectorTermStatisticsForQuery);
                Map<String, Double> sortedDocIdOkapiValuesMap = MapUtils.sortByValue(docIdOkapiValuesMap);
                QueryResultWriter.writeQueryResultToFile(query, sortedDocIdOkapiValuesMap, okapiOutputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void calculateTfIdf(List<Query> queryList, String tfIdfOutputFile) {
        for (Query query : queryList) {
            System.out.println("\n\nCalculating TF-IDF for: " + query.getCleanedQuery());
            try {
//                Map<String, List<TermStatistics>> termStatistics = StatisticsProvider.getTermStatisticsForQuery(query);
//                Map<String, Double> docIdTfIdfValuesMap = TfIdfCalculator.tfidf(termStatistics);
                Map<String, List<VectorStatistics>> vectorTermStatisticsForQuery = StatisticsProvider.getVectorTermStatisticsForQuery(query);
                Map<String, Double> docIdTfIdfValuesMap = TfIdfCalculator.tfidf_from_es(vectorTermStatisticsForQuery);
                Map<String, Double> sortedDocIdOkapiValuesMap = MapUtils.sortByValue(docIdTfIdfValuesMap);
                QueryResultWriter.writeQueryResultToFile(query, sortedDocIdOkapiValuesMap, tfIdfOutputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void calculateOkapiBM25(List<Query> queryList, String bm25OutputFile) {
        for (Query query : queryList) {
            System.out.println("\n\nCalculating BM25 for: " + query.getCleanedQuery());
            try {
//                Map<String, List<TermStatistics>> termStatistics = StatisticsProvider.getTermStatisticsForQuery(query);
//                Map<String, Double> docIdBm25ValuesMap = BM25Calculator.bm25(termStatistics, query);
                Map<String, List<VectorStatistics>> vectorTermStatisticsForQuery = StatisticsProvider.getVectorTermStatisticsForQuery(query);
                Map<String, Double> docIdBm25ValuesMap = BM25Calculator.bm25_from_es(vectorTermStatisticsForQuery);
                Map<String, Double> sortedDocIdBm25ValuesMap = MapUtils.sortByValue(docIdBm25ValuesMap);
                QueryResultWriter.writeQueryResultToFile(query, sortedDocIdBm25ValuesMap, bm25OutputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void calculateUnigramWithLaplaceSmoothing(List<Query> queryList, String unigramWithLaplaceSmoothingOutputFile) {
        int vocabularySize = Integer.parseInt(ConfigurationManager.getConfigurationValue("corpus.vocabulary.size"));
        DocumentIdExtractor extractor = new DocumentIdExtractor();
        Set<String> allDocumentIds = null;
        try {
            allDocumentIds = extractor.getAllDocumentIds();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Query query : queryList) {
            System.out.println("\n\nCalculating Unigram LM with Laplace smoothing for: " + query.getCleanedQuery());
            Map<String, Double> docIdFinalLaplaceValue = new HashMap<>();
            int termsInQuery = query.getCleanedQuery().split(" ").length;
            double defaultValue = termsInQuery * Math.log(1 / vocabularySize);
            for (String id : allDocumentIds) {
                docIdFinalLaplaceValue.put(id, defaultValue);
            }
            try {
                Map<String, List<TermStatistics>> termStatistics = StatisticsProvider.getTermStatisticsForQuery(query);
                Map<String, Double> docIdUnigramValuesMap = UnigramWithLaplaceSmoothingCalculator.lm_laplace(termStatistics, docIdFinalLaplaceValue);
                Map<String, Double> sortedDocIdUnigramValuesMap = MapUtils.sortByValue(docIdUnigramValuesMap);
                QueryResultWriter.writeQueryResultToFile(query, sortedDocIdUnigramValuesMap, unigramWithLaplaceSmoothingOutputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void calculateUnigramWithJeliekSmoothing(List<Query> queryList, String unigramWithJelinekSmoothingOutputFile) {
        DocumentIdExtractor extractor = new DocumentIdExtractor();
        Set<String> allDocumentIds = null;
        try {
            allDocumentIds = extractor.getAllDocumentIds();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Query query : queryList) {
            System.out.println("\n\nCalculating Unigram LM with Jelinek Mercer smoothing for: " + query.getCleanedQuery());
            try {
                Map<String, Double> finalDocIdJmValuesForQuery = StatisticsProvider.getTermStatisticsForQueryJelinek(query, allDocumentIds);
                Map<String, Double> sortedFinalDocIdJmValuesForQuery = MapUtils.sortByValue(finalDocIdJmValuesForQuery);
                QueryResultWriter.writeQueryResultToFile(query, sortedFinalDocIdJmValuesForQuery, unigramWithJelinekSmoothingOutputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
