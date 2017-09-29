package app.crawler;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.ConfigurationManager;
import app.models.CrawlableURL;
import app.models.DocumentModel;
import app.crawler.strategy.LinkSelectorProvider;
import app.crawler.strategy.SeedURLProvider;
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
    private static final String OUTPUT_DIR = ConfigurationManager.getConfigurationValue("model.files.output.dir");
    private static final long POLITENESS_TIMEOUT = Long.parseLong(ConfigurationManager.getConfigurationValue("politeness.timeout"));
    private static final String outlinksOutputFilePath = ConfigurationManager.getConfigurationValue("out.linkmap.output.file");
    private static final String inlinksMapOutputFilePath = ConfigurationManager.getConfigurationValue("in.linkmap.output.file");
    private static final String logFilePath = ConfigurationManager.getConfigurationValue("log.file.path");

    private static final int MAX_URL_CRAWL_COUNT = Integer.parseInt(ConfigurationManager.getConfigurationValue("max.url.crawl.count"));
    // frontier
    private static PriorityQueue<CrawlableURL> frontier = new PriorityQueue<>(5, new Comparator<CrawlableURL>() {
        @Override
        public int compare(CrawlableURL curl1, CrawlableURL curl2) {
            return curl2.getScore() - curl1.getScore();
        }
    });
    // out links map <url : <out links from url>>
    private static Map<CrawlableURL, Set<CrawlableURL>> outlinksMap = new HashMap<>();

    // domain name politeness timeout map
    private static Map<String, Long> domainTimeMap = new HashMap<>();

    // all visited URLs
    private static Set<CrawlableURL> visitedURLs = new HashSet<>();
    // map of depth/wave and links found in that wave
    private static Map<Integer, Set<CrawlableURL>> depthLinksMap = new HashMap<>();

    private static JsonFactory jsonFactory = new JsonFactory();
    private static FileOutputStream fileOutputStream = null;
    private static JsonGenerator jsonGenerator = null;
    ///////////////////////////////////////////

    public static void crawl(final CrawlableURL curl, int nextDepth) {
        LOG.info("URL no = [" + (visitedURLs.size() + 1) + "], currentDepth = [" + (nextDepth - 1) + "], " + "Crawling: " + curl.getOriginalUrl().toString());
        try {
            String url = curl.getOriginalUrl().toString();

            if (!RobotsTxtReader.isUrlAllowed(url)) {
                LOG.warn("[" + curl + "] not allowed to be crawled according to Robot.txt rules.");
                return;
            }

            String hostName = curl.getOriginalUrl().getHost();
            // use domain specific css selector to get anchor tags
            String selector = LinkSelectorProvider.getAnchorSelector(hostName);

            Connection.Response response = Jsoup.connect(url)
                    .timeout(10 * 1000)
                    .header("Accept-Language", "en")
                    .header("Accept-Encoding", "gzip, deflate")
                    .maxBodySize(0)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .execute();

            if (response.statusCode() != 200) {
                LOG.warn("[" + curl + "] returned status code =[" + response.statusCode() + "]. Ignoring...");
                return;
            }
            // update delay
            domainTimeMap.put(hostName, System.currentTimeMillis());
            Document document = response.parse();
            // get http headers
            Map<String, String> headersMap = response.headers();
            final String rawHtml = document.html();
            final String content = document.text();
            final String title = document.title();

            // get links from this page
            Elements outlinks = document.select(selector);

            // create inlinks and outlinks map
            Set<CrawlableURL> crawlableUrlOutlinks = URLUtils.getCrawlableUrls(outlinks, nextDepth, curl);

            if (crawlableUrlOutlinks.size() > 0) {
                outlinksMap.put(curl, crawlableUrlOutlinks);
                if (depthLinksMap.containsKey(nextDepth)) {
                    Set<CrawlableURL> nextCrawlableURLS = depthLinksMap.get(nextDepth);
                    nextCrawlableURLS.addAll(crawlableUrlOutlinks);
                    depthLinksMap.put(nextDepth, nextCrawlableURLS);
                } else {
                    depthLinksMap.put(nextDepth, crawlableUrlOutlinks);
                }
            }

            List<String> outlinksList = new ArrayList<>();
            crawlableUrlOutlinks.stream().forEach(link -> outlinksList.add(link.getCanonicalizedUrl()));


            LOG.info("Adding [" + crawlableUrlOutlinks.size() + "] URLs to map at depth = [" + nextDepth + "]");

            DocumentModel model = new DocumentModel(curl.getCanonicalizedUrl(), headersMap, title, content, rawHtml,
                    null, outlinksList, "Abhishek", nextDepth - 1, curl.getOriginalUrl().toString());
            jsonGenerator.writeObject(model);

            visitedURLs.add(curl);
            LOG.info("Current depth = [" + (nextDepth - 1) + "], Done crawling = [" + curl + "]\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int fileNumberCounter = 1;

        List<CrawlableURL> seedUrls = SeedURLProvider.getSeedUrls();
        Set<CrawlableURL> set = new HashSet<>();
        set.addAll(seedUrls);
        int nextDepth = 1;
        // depth 0 -> seed urls
        depthLinksMap.put(0, set);
        frontier.addAll(seedUrls);
        LOG.info("Added [" + seedUrls.size() + "] seed URLs to frontier.\n" + seedUrls + "\n\n");
        createJsonGenerator(fileNumberCounter);

        while (visitedURLs.size() < MAX_URL_CRAWL_COUNT) {
            if (visitedURLs.size() % 1000 == 0) {
                fileNumberCounter += 1;
                createJsonGenerator(fileNumberCounter);
            }

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

            } else {
                CrawlableURL urlToCrawl = frontier.poll();
                String hostName = urlToCrawl.getOriginalUrl().getHost();
                checkPolitenessTimeout(hostName);
                crawl(urlToCrawl, nextDepth);
            }
        }
        LOG.info("=========================================================================\n\n");
        LOG.info("Crawling done. Writing [" + outlinksMap.size() + "] outlinks to file. \n\n");
//        writeLinkMapToFile(outlinksMap, outlinksOutputFilePath);
    }

    private static void createJsonGenerator(final int fileNumberCounter) {
        try {
            File opFile = new File(OUTPUT_DIR + "/" + fileNumberCounter + ".json");
            fileOutputStream = new FileOutputStream(opFile, true);
            jsonGenerator = jsonFactory.createJsonGenerator(fileOutputStream, JsonEncoding.UTF8);
            jsonGenerator.setPrettyPrinter(new MinimalPrettyPrinter("\n"));
            jsonGenerator.setCodec(new ObjectMapper());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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


    private static void writeLinkMapToFile(Map<CrawlableURL, Set<CrawlableURL>> linkMap, String linksOutputFilePath) {
        File file = new File(linksOutputFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
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
                builder.append(" ").append(oulink.getOriginalUrl());
            }
            builder.append("\n");
            String data = builder.toString();
            writer.write(data);
            writer.flush();
        }

        writer.close();
    }


}
