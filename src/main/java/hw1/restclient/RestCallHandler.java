package hw1.restclient;

import hw1.main.ConfigurationManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by Abhishek Mulay on 5/17/17.
 */
public class RestCallHandler {

    static Logger LOG = LogManager.getLogger(RestCallHandler.class);
    private String serverAddress = ConfigurationManager.getConfigurationValue("team.elastic.server.address");
    private String index = ConfigurationManager.getConfigurationValue("team.elastic.index");
    private String type = ConfigurationManager.getConfigurationValue("team.elastic.type");

    private String TEAM_ELASTIC_ENDPOINT = serverAddress + "/" + index + "/" + type + "/";

    private final String INDEX_NAME = ConfigurationManager.getConfigurationValue("index.name");
    private final String TYPE_NAME = ConfigurationManager.getConfigurationValue("type.name");
//    private final String BULK_API_ENDPOINT = '/' + INDEX_NAME + '/' + INDEX_NAME + "/_bulk";

    private final String BULK_API_ENDPOINT =  '/' + index + '/' + index+ "/_bulk";
    private final String DOCUMENT_API = '/' + INDEX_NAME + '/' + TYPE_NAME + '/';

    private HttpHost host = null;
    private RestClient restClient = null;

    public void openConnection() {
        this.host = new HttpHost("localhost", 9200, "http");
        this.restClient = RestClient.builder(host).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                    @Override
                    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                        return requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(60000);
                    }
                }).setMaxRetryTimeoutMillis(60000).build();
    }
    public void openConnection(final String hostUrlOrIP, final int port) {
        this.host = new HttpHost(hostUrlOrIP, port, "http");
        this.restClient = RestClient.builder(host).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(60000);
            }
        }).setMaxRetryTimeoutMillis(60000).build();
    }

    public void closeConnection() {
        try {
            this.restClient.close();
            this.restClient = null;
            this.host = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Response get(final String body, final String endPoint) {
        HttpEntity entity = new NStringEntity(body, ContentType.APPLICATION_JSON);
        Response response = null;
        try {
            response = restClient.performRequest("GET", endPoint, Collections.singletonMap("pretty", "true"), entity);
            LOG.info(endPoint + " | STATUS: " + response.getStatusLine().getStatusCode() + " " + response
                    .getStatusLine().getReasonPhrase());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public Response post(final String body, final String endPoint) {
        Response response = null;
        HttpEntity entity = new NStringEntity(body, ContentType.APPLICATION_JSON);
        try {
            response = restClient.performRequest("POST", endPoint, Collections.<String, String>emptyMap(), entity);
            LOG.info(endPoint + " | Status: " + response.getStatusLine().getStatusCode() + " " + response
                    .getStatusLine().getReasonPhrase());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public Response bulkPOST(String bulkRequestBody) {
        HttpEntity entity = new NStringEntity(bulkRequestBody, ContentType.APPLICATION_JSON);
        Response response = null;
        try {
            response = restClient.performRequest("POST", BULK_API_ENDPOINT, Collections.<String, String>emptyMap(), entity);
            LOG.info(BULK_API_ENDPOINT + " | STATUS: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public Response head(final String endPoint) {
        Response response = null;
//        HttpEntity entity = new NStringEntity(body, ContentType.APPLICATION_JSON);
        try {
            response = restClient.performRequest("HEAD", endPoint, Collections.<String, String>emptyMap());
            LOG.info(endPoint + " | Status: " + response.getStatusLine().getStatusCode() + " " + response
                    .getStatusLine()
                    .getReasonPhrase());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }


    public String getMetadata(String indexName, String typeName, String documentId) {
        String actionMetaData = String.format
                ("{ \"index\" : { \"_index\" : \"%s\", \"_type\" : \"%s\", \"_id\" : \"%s\" } }%n",
                        indexName, typeName, documentId);
        return actionMetaData;
    }

}
