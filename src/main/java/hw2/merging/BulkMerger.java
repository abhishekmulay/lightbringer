package hw2.merging;

import hw1.main.ConfigurationManager;
import util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public List<File> getAllCatalogFiles() {
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

        while (true) {
            List<File> allCatalogFiles = getAllCatalogFiles();
            // last catalog remaining.
            if (allCatalogFiles.size() == 1) {
                break;
            }
            for (int index=0; index < (allCatalogFiles.size() - 1); index+=2) {
                File catalog1 = allCatalogFiles.get(index);
                File catalog2 = allCatalogFiles.get(index + 1);
//                IndexMerger.merge(catalog1.getPath(), catalog2.getPath());
            }
        }
        // last index and catalog are remaining. Rename them to standard names.
        renameLastCatalogAndIndex();
    }

    private void renameLastCatalogAndIndex() {
        List<File> allCatalogFiles = getAllCatalogFiles();
        File catalogFile = allCatalogFiles.get(0);
        String invertedIndexFileForCatalog = FileUtils.getInvertedIndexFileForCatalog(catalogFile.getPath());
        File indexFile = new File(invertedIndexFileForCatalog);
        try {
            Path catalogOld  = Paths.get(catalogFile.getPath());
            Files.move(catalogOld, catalogOld.resolveSibling("index_catalog.txt"));

            Path indexOld  = Paths.get(indexFile.getPath());
            Files.move(indexOld, indexOld.resolveSibling("index.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
