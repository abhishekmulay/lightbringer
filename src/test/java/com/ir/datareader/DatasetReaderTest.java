package com.ir.datareader;

import com.ir.models.HW1Model;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Created by Abhishek Mulay on 5/11/17.
 */
public class DatasetReaderTest extends TestCase {
    private static String TEST_FILE_PATH = "/Users/abhishek/Google " +
            "Drive/NEU/summer-17/IR/IR_data/AP_DATA/ap89_collection/ap890101";
    private DatasetReader reader;
    public void setUp() throws Exception {
        super.setUp();
        reader = new DatasetReader();
    }

    @Test
    public void testSingleFile() {
        File testFile = new File(TEST_FILE_PATH);
        List<HW1Model> hw1Models = reader.readFileIntoModel(testFile);
        HW1Model model = hw1Models.get(0);
        System.out.println(model);
    }

    public void tearDown() throws Exception {
    }

}