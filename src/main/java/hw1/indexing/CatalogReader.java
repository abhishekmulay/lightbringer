package hw1.indexing;

import hw1.main.ConfigurationManager;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * Created by Abhishek Mulay on 6/4/17.
 */
public class CatalogReader {

    public static void read(final String invertedIndexFilePath, int position, int offset) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(invertedIndexFilePath, "r");
            // move file pointer to position from where we want to read
            randomAccessFile.seek(position);
            byte [] entryForTerm = new byte[offset];
            randomAccessFile.read(entryForTerm, 0, entryForTerm.length);
            String entry = new String(entryForTerm, StandardCharsets.UTF_8);
            System.out.println("Read from index: " + entry);
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final String INVERTED_INDEX_FOLDER = ConfigurationManager.getConfigurationValue("inverted.index.files.directory");
        final String catalogFilePath = INVERTED_INDEX_FOLDER + "1_catalog.txt";
        final String invertedIndexFilePath = INVERTED_INDEX_FOLDER + "1.txt";
        int position  = 1966;
        int offset = 42;

        // coals:1966:42
        read(invertedIndexFilePath, position, offset);
    }

}
