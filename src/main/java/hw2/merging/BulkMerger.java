package hw2.merging;

import hw1.main.ConfigurationManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek Mulay on 6/6/17.
 */
public class BulkMerger {
    final static String INVERTED_INDEX_FOLDER = ConfigurationManager.getConfigurationValue("inverted.index.files.directory");

    public BulkMerger(String INVERTED_INDEX_FOLDER) {
        File dir = new File(INVERTED_INDEX_FOLDER);
        int numOfFiles = dir.listFiles().length;
        if (numOfFiles < 1) {
            throw new IllegalStateException("Can not use Bulk Merger. Inverted Index Files are not available in ["+ dir +"]");
        }
    }

    private List<File> getAllCatalogFiles() {
        File dir = new File(INVERTED_INDEX_FOLDER);
        File[] allFiles = dir.listFiles();
        List<File> catalogList = new ArrayList<>();
        for (File file : allFiles) {
            if (file.getName().contains("_catalog"))
                catalogList.add(file);
        }
        return catalogList;
    }

    public void bulkMerge() {
        List<File> allCatalogFiles = getAllCatalogFiles();
        if (allCatalogFiles.size() < 2) {
            // base case
            return;
        }else {
            System.out.println("\nMerging " + allCatalogFiles.size() + " index files.");
            for (int index=0; index < allCatalogFiles.size()-1; index+=2) {
                File firstCatalogFile = allCatalogFiles.get(index);
                File secondCatalogFile = allCatalogFiles.get(index+1);
                InvertedIndexFileMerger.merge(firstCatalogFile.getName(), secondCatalogFile.getName());
            }
            // recur
            bulkMerge();
        }

    }
}
