package hw3.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import hw1.restclient.RestCallHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Abhishek Mulay on 6/26/17.
 */

//
//"mappings": {
//        "document": {
//        "properties": {
//        "docno": {
//        "type": "text",
//        "store": "true",
//        "index": "analyzed",
//        "term_vector": "with_positions_offsets_payloads"
//        },
//        "HTTPheader": {
//        "type": "text",
//        "store": "true",
//        "index": "not_analyzed"
//        },
//        "title": {
//        "type": "text",
//        "store": "true",
//        "index": "analyzed",
//        "term_vector": "with_positions_offsets_payloads"
//        },
//        "text": {
//        "type": "text",
//        "store": "true",
//        "index": "analyzed",
//        "term_vector": "with_positions_offsets_payloads"
//        },
//        "html_Source": {
//        "type":"text",
//        "store": "true",
//        "index": "no"
//        },
//        "in_links": {
//        "type": "text",
//        "store": "true",
//        "index": "no"
//        },
//        "out_links": {
//        "type": "text",
//        "store": "true",
//        "index": "no"
//        },
//        "author": {
//        "type": "keyword",
//        "store": "true",
//        "index": "analyzed"
//        },
//        "depth": {
//        "type": "integer",
//        "store": "true"
//        },
//        "url": {
//        "type": "keyword",
//        "store": "true"
//        }
//        }
//        }
//        }


public class DocumentModel {
    final String docno;
    final Map<String, String> HTTPheader;
    final String title;
    final String text;
    final String html_Source;
    final List<String> in_links;
    final List<String> out_links;
    final String author;
    final int depth;
    final String url;

    public DocumentModel(String docno, Map<String, String> HTTPheader, String title, String text, String html_Source, List<String> in_links, List<String> out_links, String author, int depth, String url) {
        this.docno = docno;
        this.HTTPheader = HTTPheader;
        this.title = title;
        this.text = text;
        this.html_Source = html_Source;
        this.in_links = in_links;
        this.out_links = out_links;
        this.author = author;
        this.depth = depth;
        this.url = url;
    }

    public String getDocno() {
        return docno;
    }

    public Map<String, String> getHTTPheader() {
        return HTTPheader;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getHtml_Source() {
        return html_Source;
    }

    public List<String> getIn_links() {
        return in_links;
    }

    public List<String> getOut_links() {
        return out_links;
    }

    public String getAuthor() {
        return author;
    }

    public int getDepth() {
        return depth;
    }

    public String getUrl() {
        return url;
    }

    public static void main(String[] args) {
        DocumentModel model = new DocumentModel(
                "https://en.wikipedia.org/wiki/Stressed_skin",
                Collections.emptyMap(),
                "Stressed Skin",
                "Some text from page",
                "<html> HTML source of page.</html>",
                Collections.emptyList(),
                Collections.emptyList(),
                "Abhishek",
                0,
                "https://en.wikipedia.org/wiki/Stressed_skin"
        );

        ObjectWriter writer = new ObjectMapper().writer();
        String json = "";
        try {
            json = writer.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        RestCallHandler handler = new RestCallHandler();
        handler.openConnection();
        String endpoint = "";
        handler.post(json, endpoint);
        handler.closeConnection();

    }
}

