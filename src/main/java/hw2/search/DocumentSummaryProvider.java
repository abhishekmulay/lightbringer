package hw2.search;

import hw1.indexing.datareader.DocumentSummary;
import hw1.main.ConfigurationManager;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Abhishek Mulay on 6/9/17.
 */
public class DocumentSummaryProvider {
    private static final String DOCUMENT_SUMMARY_FILE = ConfigurationManager.getConfigurationValue("document.summary.file");

    private static Map<Integer, DocumentSummary> intDocSummaryMap = new HashMap<>();
    private static Map<String, DocumentSummary> originalDocIdSummaryMap = new HashMap<>();

    static {
        initializeMaps();
    }

    public DocumentSummaryProvider() {
        this.initializeMaps();
    }

    private static void initializeMaps() {
        try {
            InputStream content = new FileInputStream(DOCUMENT_SUMMARY_FILE);
            BufferedReader br = new BufferedReader(new InputStreamReader(content));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] token = line.split(" ");
                int docIdMappingNumber = Integer.parseInt(token[0]);
                String documentId = token[1];
                int docLength = Integer.parseInt(token[2]);
                DocumentSummary documentSummary = new DocumentSummary(documentId, docIdMappingNumber, docLength);
                intDocSummaryMap.put(docIdMappingNumber, documentSummary);
                originalDocIdSummaryMap.put(documentId, documentSummary);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getOriginalDocumentId(final int docIdMappingNumber) {
        DocumentSummary documentSummary = intDocSummaryMap.get(docIdMappingNumber);
        return documentSummary.getDocumentId();
    }

    public static int getDocIdMappingNumber(final String documentId) {
        DocumentSummary documentSummary = originalDocIdSummaryMap.get(documentId);
        return documentSummary.getDocIdMappingNumber();
    }

    public static int getDocumentLength(final String documentId) {
        DocumentSummary documentSummary = originalDocIdSummaryMap.get(documentId);
        return documentSummary.getDocumentLength();
    }

    public static Set<String> getAllDocumentIds() {
        Set<String> documentIds = new HashSet<>();
        originalDocIdSummaryMap.entrySet().forEach((Map.Entry<String, DocumentSummary> entry) -> {
            String documentId = entry.getKey();
            documentIds.add(documentId);
        });
        return documentIds;
    }
}
