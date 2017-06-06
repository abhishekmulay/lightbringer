package hw2.merging;

import hw1.main.ConfigurationManager;
import hw2.indexing.CatalogEntry;
import hw2.indexing.CatalogReader;
import hw2.indexing.Indexer;
import org.omg.IOP.ENCODING_CDR_ENCAPS;

import javax.management.StringValueExp;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Abhishek Mulay on 6/5/17.
 */
public class InvertedIndexFileMerger {

    final static String INVERTED_INDEX_FOLDER = ConfigurationManager.getConfigurationValue("inverted.index.files.directory");
    final static String INVERTED_INDEX_RECORD_SEPARATOR = ConfigurationManager.getConfigurationValue("inverted.index" +
            ".file.record.sepatator");

    // Takes in two catalog files and merges the inverted index files corresponding to those catalogs into a new
    // inverted index and a catalog.

    public static void merge(final String catalogFile1, final String catalogFile2) {
        System.out.println("Merging [" + catalogFile1 + "] and [" + catalogFile2 + "]");

        String invertedIndexFile1 = getInvertedIndexFileForCatalog(catalogFile1);
        String invertedIndexFile2 = getInvertedIndexFileForCatalog(catalogFile2);

        String mergedInvertedIndexFilePath = getMergedFilePath(invertedIndexFile1, invertedIndexFile2);
        String mergedCatalogFilePath = getMergedFilePath(catalogFile1, catalogFile2);

        // need to getCatalogAsMap catalog files into a map in memory
        Map<String, CatalogEntry> catalogMap1 = CatalogReader.getCatalogAsMap(catalogFile1);
        Map<String, CatalogEntry> catalogMap2 = CatalogReader.getCatalogAsMap(catalogFile2);

        StringBuffer buffer = new StringBuffer();
        int position = 0;
        int offset = 0;

        // catalog for newly merged index files created.
        Map<String, CatalogEntry> mergedCatalog = new HashMap<>();

        // loop over for each term
        for (Map.Entry<String, CatalogEntry> entry1 : catalogMap1.entrySet()) {
            String term = entry1.getKey();
            String lineFromIndex1 = "";

            // where is this entry going to be written, bytes
            position = buffer.length();

            CatalogEntry catalogEntry1 = entry1.getValue();
            lineFromIndex1 = CatalogReader.read(invertedIndexFile1, catalogEntry1.getPosition(), catalogEntry1.getOffset());

            // the term is present in both catalogs
            if (catalogMap2.containsKey(term)) {
                CatalogEntry catalogEntry2 = catalogMap2.get(term);
                String lineFromIndex2 = CatalogReader.read(invertedIndexFile2, catalogEntry2.getPosition(), catalogEntry2.getOffset());

                String mergedEntry = mergeEntries(lineFromIndex1, lineFromIndex2);
                buffer.append(mergedEntry);
            } else {
                buffer.append(lineFromIndex1);
            }
            buffer.append("\n");

            offset = buffer.length() - position;
            mergedCatalog.put(term, new CatalogEntry(term, position, offset));
        }

        // after these two loops, map1 will get over, there might be entries left in map2.
        for (Map.Entry<String, CatalogEntry> entry2 : catalogMap2.entrySet()) {
            String term = entry2.getKey();
            position = buffer.length();

            // if the term is not present in mergedCatalog then add it
            if (!mergedCatalog.containsKey(term)) {
                CatalogEntry catalogEntry2 = entry2.getValue();
                String lineFromIndex2 = CatalogReader.read(invertedIndexFile2, catalogEntry2.getPosition(), catalogEntry2.getOffset());
                buffer.append(lineFromIndex2);
                buffer.append('\n');

                offset = buffer.length() - position;
                mergedCatalog.put(term, new CatalogEntry(term, position, offset));
            }
        }

        // create new index and catalog
        String combinedEntry = buffer.toString();
        byte[] bytes = combinedEntry.getBytes(StandardCharsets.UTF_8);

        Indexer.writeBytesToFile(bytes, mergedInvertedIndexFilePath);
        Indexer.createCatalogFile(mergedCatalog, mergedCatalogFilePath);
    }

    public static String getMergedFilePath(String file1, String file2) {
        if (file1.isEmpty() || file2.isEmpty() || file1 == null || file2 == null)
            throw new IllegalArgumentException("Illegal file parameter");

        String indexFileName = file1.replace(INVERTED_INDEX_FOLDER, "").replace(".txt", "");
        String indexFileName2 = file2.replace(INVERTED_INDEX_FOLDER, "").replace(".txt", "");

        if (indexFileName.contains("_catalog")) {
            indexFileName = indexFileName.replace("_catalog", "");
        }

        String mergedFilePath = INVERTED_INDEX_FOLDER + indexFileName + "_" + indexFileName2 + ".txt";
        return mergedFilePath;
    }


    // 1) remove line breakes from both lines
    // 2) remove term from first line and get remaining record as substring
    // 3) append the record from line1 to end of line2
    public static String mergeEntries(String line1, String line2) {
        // remove line break, line break will be added while being written to file.
        line1 = line1.replace("\n", "").replace("\r", "").trim();
        line2 = line2.replace("\n", "").replace("\r", "").trim();

        int indexOfTerm1 = line1.indexOf("=");
        String record1 = line1.substring(indexOfTerm1 + 1, line1.length());

        // check if record1 ends with separator
        String lastCharacter = String.valueOf(record1.charAt(record1.length() - 1));
        if (!lastCharacter.equals(INVERTED_INDEX_RECORD_SEPARATOR)) {
            // record 1 should have a separator at the end
            record1 += INVERTED_INDEX_RECORD_SEPARATOR;
        }

        // check if line2 ends with separator
        String lastCharacterInLineTwo = String.valueOf(line2.charAt(line2.length() - 1));
        if (!lastCharacterInLineTwo.equals(INVERTED_INDEX_RECORD_SEPARATOR)) {
            // line2 should have a separator at the end as we are going to add a new record at the end of line2
            line2 += INVERTED_INDEX_RECORD_SEPARATOR;
        }

        String combinedEntry = line2 + record1;
        return combinedEntry;
    }

    public static String getInvertedIndexFileForCatalog(final String catalogFile) {
        if (catalogFile.isEmpty() || catalogFile == null)
            throw new IllegalArgumentException("Illegal file parameter");
        String invertedIndexFile = catalogFile.replaceAll("_catalog", "");
        return invertedIndexFile;
    }

    public static String getCatalogFileForInvertedIndexFile(final String invertedIndexFilePath) {
        if (invertedIndexFilePath.isEmpty() || invertedIndexFilePath == null)
            throw new IllegalArgumentException("Illegal file parameter");
        String catalogFilePath = invertedIndexFilePath.replaceAll("\\.", "_catalog.");
        return catalogFilePath;
    }

    public static void main(String[] args) {
        String INVERTED_INDEX_FOLDER = ConfigurationManager.getConfigurationValue("inverted.index.files.directory");
        String invertedIndexFile1 = INVERTED_INDEX_FOLDER + "1.txt";
        String invertedIndexFile2 = INVERTED_INDEX_FOLDER + "2.txt";

        String catalogFilePath1 = INVERTED_INDEX_FOLDER + "1_catalog.txt";
        String catalogFilePath2 = INVERTED_INDEX_FOLDER + "2_catalog.txt";

        merge(catalogFilePath1, catalogFilePath2);
    }
}
