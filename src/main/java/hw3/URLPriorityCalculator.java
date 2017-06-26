package hw3;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Abhishek Mulay on 6/24/17.
 */
public class URLPriorityCalculator {

    private static List<String> relevantWordList = new ArrayList<>();

    private static List<String> reputedHosts = new ArrayList<>();

    private static String relevantWords  = "Earthquake, 11 March 2011, Fukushima I Nuclear Power Plant, Tokyo Electric Power Company, TEPCO,  International\n" +
            "Nuclear Event Scale, INES, Level 7,\n" +
            "Ōkuma, Fukushima, Japan, Nuclear, reactor,  zirconium, radiation-induced cancer, cancer, Zircaloy, BWR, Tsunami,\n" +
            "evacuation, core meltdown, contamination, death, fission reaction, control rods, SCRAM, decay heat, radiation, flood,\n" +
            "fatalities,";

    static {
        relevantWordList = Arrays.asList(relevantWords.toLowerCase().split(","));
        reputedHosts.add("en.wikipedia.org");
        reputedHosts.add("www.google.com");
        reputedHosts.add("fukushimaupdate.com");
    }

    public static int calculatePriority(URI originalUrl, String canonicalizedUrl, String titleKeywords) {
        int score = 1;
        String host = originalUrl.getHost();
        if (reputedHosts.contains(host)) {
            score += 100;
        }
        for (String word : relevantWordList) {
            if (titleKeywords.toLowerCase().contains(word)) {
                score *= 10;
            }
        }
        return score;
    }

}
