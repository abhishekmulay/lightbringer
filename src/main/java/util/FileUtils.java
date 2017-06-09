package util;

import hw1.main.ConfigurationManager;
import hw2.merging.InvertedIndexFileMerger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek Mulay on 6/7/17.
 */
public class FileUtils {

    final static String INVERTED_INDEX_FOLDER = ConfigurationManager.getConfigurationValue("inverted.index.files.directory");

    public static String getCatalogFileForInvertedIndexFile(final String invertedIndexFilePath) {
        if (invertedIndexFilePath.isEmpty() || invertedIndexFilePath == null)
            throw new IllegalArgumentException("Illegal file parameter");
        String catalogFilePath = invertedIndexFilePath.replaceAll("\\.", "_catalog.");
        return catalogFilePath;
    }

    public static String getInvertedIndexFileForCatalog(final String catalogFile) {
        if (catalogFile.isEmpty() || catalogFile == null)
            throw new IllegalArgumentException("Illegal file parameter");
        String invertedIndexFile = catalogFile.replaceAll("_catalog", "");
        return invertedIndexFile;
    }

    public static void copyCatalogAndIndexFilesToFolder(String catalogFile1, String catalogFile2, String destinationFolder) {
        List<File> filesToMove = new ArrayList<>();
        filesToMove.add(new File(catalogFile1));
        filesToMove.add(new File(catalogFile2));

        String invertedIndexFile1 = getInvertedIndexFileForCatalog(catalogFile1);
        String invertedIndexFile2 = getInvertedIndexFileForCatalog(catalogFile2);
        filesToMove.add(new File(invertedIndexFile1));
        filesToMove.add(new File(invertedIndexFile2));

        for (File file : filesToMove) {
            // move these files to destination folder and delete from current folder.
            try {
                Files.move(Paths.get(INVERTED_INDEX_FOLDER + file.getName()), Paths.get(destinationFolder + file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeBytesToFile(byte[] bytes, String invertedIndexFilePath) {
        try {
            File file = new File(invertedIndexFilePath);
            // create if file does not exist.
            if (!file.exists())
                file.createNewFile();
            FileOutputStream stream = new FileOutputStream(invertedIndexFilePath, true);
            stream.write(bytes);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
