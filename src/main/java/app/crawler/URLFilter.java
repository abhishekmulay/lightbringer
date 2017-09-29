package app.crawler;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Pattern;

/**
 * Created by Abhishek Mulay on 6/20/17.
 */
public class URLFilter {

    static Pattern excludePattern = Pattern.compile("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|css|js|xml|pdf|ttf|txt))$)", Pattern.CASE_INSENSITIVE);

    public static Elements filterURLs(Elements urls) {
        Elements filteredElements = new Elements();

        for (Element link : urls) {
            String href = link.attr("href");
            if (isCrawlableURL(href)) {
                filteredElements.add(link);
            }
        }
        return filteredElements;
    }


    public static boolean isCrawlableURL(final String href) {
        if (href == null || href.isEmpty()) {
            return false;
        }

        // Wikipedia specific non useful file url fragments.
        if (href.contains("/wiki/File:") || href.contains("Template_talk")) {
            return false;
        }

        // URLs matching this pattern should not be crawled.
        if (excludePattern.matcher(href).matches()) {
            return false;
        }

        return true;
    }
}


