package hw2.merging;

import hw1.main.ConfigurationManager;
import hw2.indexing.CatalogEntry;
import hw2.indexing.CatalogReader;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.Map;

/**
 * Created by Abhishek Mulay on 6/5/17.
 */
public class InvertedIndexFileMergerTest extends TestCase {
    final String INVERTED_INDEX_FOLDER = ConfigurationManager.getConfigurationValue("inverted.index.files.directory");

    public void testReadFromCatalog() {
//        salary:1161:708
        final String term = "salary";
        final int position = 1161;
        final int offset = 708;

        final String expected = "salary=AP890106-0025:2.0:-1.0:-1.0:[388.0, 433.0];AP890107-0128:2.0:-1.0:-1.0:[268.0, 302.0];AP890104-0268:2.0:-1.0:-1.0:[36.0, 73.0];AP890106-0202:1.0:-1.0:-1.0:[344.0];AP890107-0017:1.0:-1.0:-1.0:[146.0];AP890107-0002:1.0:-1.0:-1.0:[373.0];AP890107-0112:1.0:-1.0:-1.0:[62.0];AP890106-0096:5.0:-1.0:-1.0:[105.0, 453.0, 530.0, 747.0, 854.0];AP890106-0253:1.0:-1.0:-1.0:[362.0];AP890102-0062:1.0:-1.0:-1.0:[715.0];AP890102-0094:1.0:-1.0:-1.0:[128.0];AP890103-0214:1.0:-1.0:-1.0:[278.0];AP890101-0062:1.0:-1.0:-1.0:[66.0];AP890102-0017:2.0:-1.0:-1.0:[190.0, 207.0];AP890103-0125:1.0:-1.0:-1.0:[600.0];AP890103-0191:1.0:-1.0:-1.0:[263.0];AP890102-0145:1.0:-1.0:-1.0:[625.0];AP890103-0107:1.0:-1.0:-1.0:[562.0];";

        final String INVERTED_INDEX_FILE_PATH = INVERTED_INDEX_FOLDER + "/1_2.txt";
        String line = CatalogReader.read(INVERTED_INDEX_FILE_PATH, position, offset);
        Assert.assertEquals(expected, line);
    }

    // returns line2 + record1
    public void testMergeEntries() {
        // both lines have separator at the end
        final String line1 = "rival=AP890103-0122:1.0:-1.0:-1.0:[303.0];AP890102-0006:1.0:-1.0:-1.0:[476.0];";
        final String line2 = "rival=AP890105-0084:1.0:-1.0:-1.0:[24.0];AP890105-0214:1.0:-1.0:-1.0:[18.0];";
        final String expected = "rival=AP890105-0084:1.0:-1.0:-1.0:[24.0];AP890105-0214:1.0:-1.0:-1.0:[18.0];AP890103-0122:1.0:-1.0:-1.0:[303.0];AP890102-0006:1.0:-1.0:-1.0:[476.0];";

        String actual = InvertedIndexFileMerger.mergeEntries(line1, line2);
        Assert.assertEquals(expected, actual);

        // when line1 has only one record
        final String line1_1 = "rival=AP890103-0122:1.0:-1.0:-1.0:[303.0]";
        final String line2_1 = "rival=AP890105-0084:1.0:-1.0:-1.0:[24.0];AP890105-0214:1.0:-1.0:-1.0:[18.0];";
        final String expected_1 = "rival=AP890105-0084:1.0:-1.0:-1.0:[24.0];AP890105-0214:1.0:-1.0:-1.0:[18.0];AP890103-0122:1.0:-1.0:-1.0:[303.0];";

        String actual_1 = InvertedIndexFileMerger.mergeEntries(line1_1, line2_1);
        Assert.assertEquals(expected_1, actual_1);

        // when line2 does not have a separator
        final String line1_2 = "rival=AP890103-0122:1.0:-1.0:-1.0:[303.0];";
        final String line2_2 = "rival=AP890105-0084:1.0:-1.0:-1.0:[24.0]";
        final String expected_2 =  "rival=AP890105-0084:1.0:-1.0:-1.0:[24.0];AP890103-0122:1.0:-1.0:-1.0:[303.0];";

        String actual_2 = InvertedIndexFileMerger.mergeEntries(line1_2, line2_2);
        Assert.assertEquals(expected_2, actual_2);
    }
}