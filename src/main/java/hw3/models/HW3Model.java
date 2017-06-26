package hw3.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Map;
import java.util.Set;

/**
 * Created by Abhishek Mulay on 6/20/17.
 */
public class HW3Model {

    CrawlableURL crawlableURL;
    final String rawHtml;
    final String content;

    private Map<String, String> httpHeaders;
    private Set<CrawlableURL> inlinks;
    private Set<CrawlableURL> outlinks;

    // in links will be calculated at the end of the crawl


    public HW3Model(CrawlableURL crawlableURL, String rawHtml, String content, Map<String, String> httpHeaders) {
        this.crawlableURL = crawlableURL;
        this.rawHtml = rawHtml;
        this.content = content;
        this.httpHeaders = httpHeaders;
    }

    public HW3Model(CrawlableURL crawlableURL, String rawHtml, String content, Map<String, String> httpHeaders, Set<CrawlableURL> crawlableUrlOutlinks) {
        this.crawlableURL = crawlableURL;
        this.rawHtml = rawHtml;
        this.content = content;
        this.httpHeaders = httpHeaders;
        this.outlinks = crawlableUrlOutlinks;
    }

    public void addInlink(final CrawlableURL inlink) {
        this.inlinks.add(inlink);
    }

    public void addOutlink(final CrawlableURL outlink) {
        this.outlinks.add(outlink);
    }


    public String convertModelToJSON() {
//        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        ObjectWriter writer = new ObjectMapper().writer();
        String json = "";
        try {
            json = writer.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public String toString() {
        return "HW3Model{" +
                "crawlableURL=" + crawlableURL +
                ", rawHtml='" + rawHtml + '\'' +
                ", content='" + content + '\'' +
                ", inlinks=" + inlinks +
                ", outlinks=" + outlinks +
                '}';
    }

    public CrawlableURL getCrawlableURL() {
        return crawlableURL;
    }

    public void setCrawlableURL(CrawlableURL crawlableURL) {
        this.crawlableURL = crawlableURL;
    }

    public String getRawHtml() {
        return rawHtml;
    }

    public String getContent() {
        return content;
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public Set<CrawlableURL> getInlinks() {
        return inlinks;
    }

    public void setInlinks(Set<CrawlableURL> inlinks) {
        this.inlinks = inlinks;
    }

    public Set<CrawlableURL> getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(Set<CrawlableURL> outlinks) {
        this.outlinks = outlinks;
    }
}
