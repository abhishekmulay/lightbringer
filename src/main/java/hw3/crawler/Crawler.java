package hw3.crawler;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import hw1.main.ConfigurationManager;
import hw3.models.CrawlableURL;
import hw3.models.HW3Model;
import hw3.LinkSelectorProvider;
import hw3.SeedURLProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import util.URLUtils;

import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * Created by Abhishek Mulay on 6/19/17.
 */
public class Crawler {

    static Logger LOG = LogManager.getLogger(Crawler.class);
    private static final String OUTPUT_FILE = ConfigurationManager.getConfigurationValue("hw3.models.file.path");
    private static final long POLITENESS_TIMEOUT = Long.parseLong(ConfigurationManager.getConfigurationValue("politeness.timeout"));
    private static final String outlinksOutputFilePath = ConfigurationManager.getConfigurationValue("out.linkmap.output.file");
    private static final String inlinksMapOutputFilePath = ConfigurationManager.getConfigurationValue("in.linkmap" + ".output.file");
    private static final String logFilePath = ConfigurationManager.getConfigurationValue("log.file.path");

    private static final int MAX_URL_CRAWL_COUNT = 21000;
    // frontier
    private static PriorityQueue<CrawlableURL> frontier = new PriorityQueue<>(5, new Comparator<CrawlableURL>() {
        @Override
        public int compare(CrawlableURL curl1, CrawlableURL curl2) {
            return curl2.getScore() - curl1.getScore();
        }
    });

    private static Map<CrawlableURL, Set<CrawlableURL>> outlinksMap = new HashMap<>();
    private static Map<CrawlableURL, Set<CrawlableURL>> inlinksMap = new HashMap<>();

    // domain name politeness timeout map
    private static Map<String, Long> domainTimeMap = new HashMap<>();

    // all visited URLs
    private static Set<CrawlableURL> visitedURLs = new HashSet<>();
    // map of depth/wave and links found in that wave
    private static Map<Integer, Set<CrawlableURL>> depthLinksMap = new HashMap<>();

    private static JsonFactory jsonFactory = new JsonFactory();
    private static FileOutputStream file = null;
    private static JsonGenerator jsonGen = null;

    private static void checkPolitenessTimeout(final String hostName) {
        if (domainTimeMap.containsKey(hostName)) {
            long timeElapsed = System.currentTimeMillis() - domainTimeMap.get(hostName);
            if (timeElapsed < POLITENESS_TIMEOUT) {
                try {
                    Thread.sleep(POLITENESS_TIMEOUT - timeElapsed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void createOuputJSONFile() {
        try {
            File opFile = new File(OUTPUT_FILE);
            if (!opFile.exists()) {
                opFile.createNewFile();
            } else {
                opFile.delete();
                opFile.createNewFile();
            }
            file = new FileOutputStream(opFile, true);
            jsonGen = jsonFactory.createJsonGenerator(file, JsonEncoding.UTF8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        jsonGen.setCodec(new ObjectMapper());
    }


    public static HW3Model crawl(final CrawlableURL curl, int nextDepth) {
        LOG.info("URL no = [" + (visitedURLs.size() + 1) + "],\tcurrentDepth = [" + (nextDepth - 1) + "],\tCrawling:\t" + curl.getOriginalUrl().toString());
        HW3Model model = null;
        try {
            String url = curl.getOriginalUrl().toString();

            if (!RobotsTxtReader.isUrlAllowed(url)) {
                LOG.warn("[" + curl + "] not allowed to be crawled according to Robot.txt rules.");
                return null;
            }

            String hostName = curl.getOriginalUrl().getHost();
            // use domain specific css selector to get anchor tags
            String selector = LinkSelectorProvider.getAnchorSelector(hostName);

            Connection.Response response = Jsoup.connect(url)
                    .timeout(10 * 1000)
                    .followRedirects(true)
                    .userAgent("Googlebot/2.1 (+http://www.google.com/bot.html)")
                    .execute();

            if (response.statusCode() != 200) {
                LOG.warn("[" + curl + "] returned status code =[" + response.statusCode() + "]. Ignoring...");
                return null;
            }
            // update delay
            domainTimeMap.put(hostName, System.currentTimeMillis());
            Document document = response.parse();
            // get http headers
            Map<String, String> headersMap = response.headers();
            final String rawHtml = document.html();
            final String content = document.text();

            // get links from this page
            Elements outlinks = document.select(selector);

            // create inlinks and outlinks map
            Set<CrawlableURL> crawlableUrlOutlinks = URLUtils.getCrawlableUrls(outlinks, nextDepth, curl);

            if (crawlableUrlOutlinks.size() > 0) {
                outlinksMap.put(curl, crawlableUrlOutlinks);
                for (CrawlableURL outlink : crawlableUrlOutlinks) {
                    // if there are already in links for this link
                    if (inlinksMap.containsKey(outlink)) {
                        Set<CrawlableURL> previousInlinks = inlinksMap.get(outlink);
                        previousInlinks.add(curl);
                        // add curent crawl url as inlink for this page
                        inlinksMap.put(outlink, previousInlinks);
                    } else {
                        Set<CrawlableURL> inlinks = new HashSet<>();
                        inlinks.add(curl);
                        inlinksMap.put(outlink, inlinks);
                    }
                }

                if (depthLinksMap.containsKey(nextDepth)) {
                    Set<CrawlableURL> nextCrawlableURLS = depthLinksMap.get(nextDepth);
                    nextCrawlableURLS.addAll(crawlableUrlOutlinks);
                    depthLinksMap.put(nextDepth, nextCrawlableURLS);
                } else {
                    depthLinksMap.put(nextDepth, crawlableUrlOutlinks);
                }
            }

            LOG.info("Adding [" + crawlableUrlOutlinks.size() + "] URLs to map at depth = [" + nextDepth + "]");

            model = new HW3Model(curl, rawHtml, content, headersMap, crawlableUrlOutlinks);
            jsonGen.writeObject(model);
            visitedURLs.add(curl);
            LOG.info("Current depth = [" + (nextDepth - 1) + "], Done crawling = [" + curl.getOriginalUrl() + "]\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return model;
    }

    public static void main(String[] args) {
        long timeAtStart = System.nanoTime();
        ///////////////////////////////////////////////////////////////////
        cleanup();
        createOuputJSONFile();

        List<CrawlableURL> seedUrls = SeedURLProvider.getSeedUrls();
        Set<CrawlableURL> set = new HashSet<>();
        set.addAll(seedUrls);
        int nextDepth = 1;
        // depth 0 -> seed urls
        depthLinksMap.put(0, set);
        frontier.addAll(seedUrls);
        LOG.info("Added [" + seedUrls.size() + "] seed URLs to frontier.\n" + seedUrls + "\n\n");

        while (visitedURLs.size() < MAX_URL_CRAWL_COUNT) {

            if (frontier.isEmpty()) {
                Set<CrawlableURL> crawlableURLS = depthLinksMap.get(nextDepth);
                if (crawlableURLS == null || crawlableURLS.size() == 0) {
                    LOG.fatal("No outlinks found for crawling at depth=" + nextDepth);
                    continue;
                }
                frontier.addAll(crawlableURLS);
                depthLinksMap.remove(nextDepth);
                nextDepth += 1;
                LOG.info("=========================================================================\n\n");
                LOG.info("Starting new wave at depth=[" + (nextDepth - 1) + "], added [" + crawlableURLS.size() + "] " + "URLs " + " to frontier.");
//                    crawlableURLS.forEach( foundUrl-> LOG.info("Added in frontier: "+ foundUrl));

            } else {
                CrawlableURL urlToCrawl = frontier.poll();
                String hostName = urlToCrawl.getOriginalUrl().getHost();
                checkPolitenessTimeout(hostName);
                HW3Model model = crawl(urlToCrawl, nextDepth);
            }
        }

        writeLinkMapToFile(outlinksMap, outlinksOutputFilePath);
        writeLinkMapToFile(inlinksMap, inlinksMapOutputFilePath);

        // combine inlinks and outlinks for each model
        createFinalDataFile();

        ///////////////////////////////////////////////////////////////////
        long timeAtEnd = System.nanoTime();
        long elapsedTime = timeAtEnd - timeAtStart;
        double seconds = (double) elapsedTime / 1000000000.0;
        System.out.println("\nTotal time taken: " + seconds / 60.0 + " minutes");
        LOG.info("\nTotal time taken: " + seconds / 60.0 + " minutes");
    }

    public static void createFinalDataFile() {
        LOG.info("Crawling completed. Now need to read the model json file and bulk post to ES.");
    }

    private static void writeLinkMapToFile(Map<CrawlableURL, Set<CrawlableURL>> linkMap, String linksOutputFilePath) {
        File file = new File(linksOutputFilePath);
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter writer = new PrintWriter(bufferedWriter);

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<CrawlableURL, Set<CrawlableURL>> crawlableURLSetEntry : linkMap.entrySet()) {
            CrawlableURL crawlableURL = crawlableURLSetEntry.getKey();
            Set<CrawlableURL> outlinks = crawlableURLSetEntry.getValue();
            URI originalUrl = crawlableURL.getOriginalUrl();

            builder.append(originalUrl);

            for (CrawlableURL oulink : outlinks) {
                builder.append(" ").append(oulink);
            }
            builder.append("\n");
            String data = builder.toString();
            writer.write(data);
            writer.flush();
        }

        writer.close();
    }

    private static void cleanup() {
        // delete : app.log, outlinks map and models.json
        String[] filesToDelete = {OUTPUT_FILE, outlinksOutputFilePath, inlinksMapOutputFilePath};
        for (String filePath : filesToDelete) {
            File file = new File(filePath);
            if (file.exists()) {
                LOG.info("Deleting [" + filePath + "]");
                file.delete();
            }
        }
    }

//    public static void main1(String[] args) {
//        String url = "https://en.wikipedia.org/wiki/Immigration_to_the_United_States";
//        Document document= null;
//        try {
//            document = Jsoup.connect(url).get();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("All links= " + document.select("a[href]").size());
//        System.out.println("Excluded links= " + document.select(LinkSelectorProvider.defaultLinkSelector).size());
//
//        Elements filteredLinks = document.select("a[href]").not("[href^=\"#\"").not("[href$=\"pdf\"").not("[href$=\"jpg\"").not("[href$=\"jpeg\"").not("[href$=\"png\"").not("[href$=\"xml\"").not("[href$=\"gif\"");
//        System.out.println("Exluded with new query= " + filteredLinks.size());
//
//    }
}
