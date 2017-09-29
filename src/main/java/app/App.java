package app;

import app.crawler.Crawler;
import app.indexer.TeamIndexMerger;

import java.io.IOException;

/**
 * Created by Abhishek Mulay on 6/20/17.
 */
public class App {
    public static void main(String[] args) {
        Crawler.main(args);
        TeamIndexMerger merger = new TeamIndexMerger();
        try {
            merger.bulkMerge();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
