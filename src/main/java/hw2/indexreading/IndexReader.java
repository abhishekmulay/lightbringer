package hw2.indexreading;

import hw1.main.ConfigurationManager;
import hw2.indexing.CatalogEntry;
import hw2.indexing.CatalogReader;
import hw2.indexing.IndexingUnit;
import hw2.merging.IndexMerger;
import hw2.search.DocumentSummaryProvider;
import util.FileUtils;
import util.ListUtils;
import util.MapUtils;

import java.io.File;
import java.util.*;

/**
 * Created by Abhishek Mulay on 6/7/17.
 */
public class IndexReader {

    private File invertedIndexFile;
    private File catalogFile;
    final static String INVERTED_INDEX_FOLDER = ConfigurationManager.getConfigurationValue("inverted.index.files.directory");
    final static String INVERTED_INDEX_SEPARATOR = ConfigurationManager.getConfigurationValue("inverted.index.file.record.separator");
    private static DocumentSummaryProvider summaryProvider = new DocumentSummaryProvider();

    public IndexReader(String catalogFilePath) {
        this.invertedIndexFile = new File(FileUtils.getInvertedIndexFileForCatalog(catalogFilePath));
        this.catalogFile = new File(catalogFilePath);
    }

    public List<IndexingUnit> get(String term) {
        Map<String, CatalogEntry> catalog = CatalogReader.getCatalogAsMap(this.catalogFile.getPath());
        if (!catalog.containsKey(term)) {
            System.out.println("Term [" + term + "] is not present in data set.");
            return null;
        }
        CatalogEntry catalogEntry = catalog.get(term);
        int position = catalogEntry.getPosition();
        int offset = catalogEntry.getOffset();

        String record = CatalogReader.read(this.invertedIndexFile.getPath(), position, offset);
        Map<String, List<IndexingUnit>> stringListMap = parseIndexEntry(record);
        return stringListMap.get(term);
    }

    // term=df1;ttf1;docIdMappingNumber1:tf1:pos1,pos2,pos3];docIdMappingNumber2:tf2:pos1,pos2,pos3];docIdMappingNumber3:tf3:pos1,pos2,pos3];
    public static Map<String, List<IndexingUnit>> parseIndexEntry(String entry) {
        String term = entry.substring(0, entry.indexOf("="));
        String docBlocksString = entry.substring(entry.indexOf("=") + 1, entry.length());
        String[] docBlocks = docBlocksString.split(INVERTED_INDEX_SEPARATOR);
        Map<String, List<IndexingUnit>> termIndexingUnitListMap = new HashMap<>();
        final int df = Integer.parseInt(docBlocks[0]);
        final int ttf = Integer.parseInt(docBlocks[1]);

        List<IndexingUnit> indexingUnitList = new ArrayList<>();
        // first two indexes are df and ttf, 0=df, 1=ttf
        for (int index=2; index<docBlocks.length; index++) {
            String block = docBlocks[index];
            String[] indexingUnitParts = block.split(":");
            try {
                int docIdMappingNumber = Integer.parseInt(indexingUnitParts[0]);
                String documentId = DocumentSummaryProvider.getOriginalDocumentId(docIdMappingNumber);
                int termFrequency = Integer.parseInt(indexingUnitParts[1]);

                List<Integer> asList = new ArrayList<>();
                int[] positionArray = ListUtils.fromString(indexingUnitParts[2]);
                for (int i : positionArray) {
                    asList.add(i);
                }

                indexingUnitList.add(new IndexingUnit(term, documentId, docIdMappingNumber, termFrequency, asList, ttf, df));
            } catch (Exception e) {
                System.out.println("Exception while parsing term: [" + term + "]");
                e.printStackTrace();
            }
        }
        termIndexingUnitListMap.put(term, indexingUnitList);
        return termIndexingUnitListMap;
    }


    // term=df1;ttf1;docIdMappingNumber1:tf1:pos1,pos2,pos3];docIdMappingNumber2:tf2:pos1,pos2,pos3];docIdMappingNumber3:tf3:pos1,pos2,pos3];

    // each line is => cancel=AP890103-0105:1:-1:-1:[239];AP890103-0176:1:-1:-1:[142];
    public static String getMergedLineForTerm(String term, List<String> linesForTerm) {
        if (linesForTerm.size() < 1) {
            System.out.println("No lines for term = [" + term +"]");
            return "";
        }

        List<IndexingUnit> indexingUnitList = new ArrayList<>();

        int df =0, ttf =0;
        for (int index=0; index < linesForTerm.size(); index++) {
            String line = linesForTerm.get(index);

            String dfTtfAndDocBlocksString = line.substring(line.indexOf("=") + 1, line.length());
            String[] docBlocks = dfTtfAndDocBlocksString.split(";");
            df += Integer.parseInt(docBlocks[0]);
            ttf += Integer.parseInt(docBlocks[1]);

            line = line.replace("\n", "").replace("\r", "").trim();
            Map<String, List<IndexingUnit>> parsedTermIndexingUnitMap = parseIndexEntry(line);
            indexingUnitList.addAll(parsedTermIndexingUnitMap.get(term));
        }

        Collections.sort(indexingUnitList, (o1, o2) -> {
            if (o1.getTermFrequency() > o2.getTermFrequency()) {
                return -1;
            } else if (o1.getTermFrequency() < o2.getTermFrequency()) {
                return 1;
            } else {
                return 0;
            }
        });

        StringBuilder builder = new StringBuilder();
        builder.append(term).append("=").append(df).append(";").append(ttf).append(";");
        for (IndexingUnit docBlock : indexingUnitList) {
            builder.append(docBlock.toString());
        }
        return builder.toString();
    }


    // line should be of like => cancel=AP890103-0105:1:-1:-1:[239];AP890103-0176:1:-1:-1:[142];
    public static String mergeEntries(String line1, String line2) {
        line1 = line1.replace("\n", "").replace("\r", "").trim();
        line2 = line2.replace("\n", "").replace("\r", "").trim();

        String term = line1.substring(0, line1.indexOf("="));
        Map<String, List<IndexingUnit>> parsedTermIndexingUnitMap1 = parseIndexEntry(line1);
        Map<String, List<IndexingUnit>> parsedTermIndexingUnitMap2 = parseIndexEntry(line2);

        List<IndexingUnit> mergedRecords = mergeIndexingUnitsList(parsedTermIndexingUnitMap1.get(term), parsedTermIndexingUnitMap2.get(term));
        // update df and ttf of records.
        mergedRecords = updateRecordValues(mergedRecords);
        // convert into string representation
        String mergedLine = IndexingUnit.toWritableString(term, mergedRecords);
        return mergedLine;
    }

    private static List<IndexingUnit> updateRecordValues(List<IndexingUnit> mergedRecords) {
        final int df = mergedRecords.size();
        int ttf = 0;
        for (IndexingUnit unit : mergedRecords) {
            ttf += unit.getTermFrequency();
        }
        for (IndexingUnit unit : mergedRecords) {
            unit.setTtf(ttf);
            unit.setDocumentFrequency(df);
        }
        return mergedRecords;
    }

    private static List<IndexingUnit> mergeIndexingUnitsList(List<IndexingUnit> list1, List<IndexingUnit> list2) {
        List<IndexingUnit> mergedIndexingUnitsList = new ArrayList<>();
        int i = 0, j = 0;
        int list1Length = list1.size();
        int list2Length = list2.size();

        while (i < list1Length && j < list2Length) {
            IndexingUnit item1 = list1.get(i);
            IndexingUnit item2 = list2.get(j);
            if (item1.getTermFrequency() > item2.getTermFrequency()) {
                mergedIndexingUnitsList.add(item1);
                i++;
            } else {
                mergedIndexingUnitsList.add(item2);
                j++;
            }
        }

        if (i < list1Length) {
            while (i < list1Length) {
                mergedIndexingUnitsList.add(list1.get(i));
                i++;
            }
        }

        if (j < list2Length) {
            while (j < list2Length) {
                mergedIndexingUnitsList.add(list2.get(j));
                j++;
            }
        }

        return mergedIndexingUnitsList;
    }


    public static void main(String[] args) {
        IndexReader reader = new IndexReader("/Users/abhishek/Google " +
                "Drive/NEU/summer-17/IR/IR_data/AP_DATA/output_files/inverted_index/1_catalog.txt");
        List<IndexingUnit> indexingUnitList = reader.get("sadden");
        for (IndexingUnit unit : indexingUnitList)
            System.out.println(unit.toPrettyString());
    }
}
