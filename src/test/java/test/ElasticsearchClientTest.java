package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.opensearch.testcontainers.OpensearchContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class ElasticsearchClientTest {

  static final String ELASTICSEARCH_USERNAME = "elastic";
  static final String ELASTICSEARCH_PASSWORD = "changeme";

  @Container
  ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
      DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.15.0"));

  @Container
  OpensearchContainer<?> opensearch = new OpensearchContainer<>(
      DockerImageName.parse("opensearchproject/opensearch:2.16.0"));

  @Test
  void elasticsearch() throws IOException {
    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(
        AuthScope.ANY,
        new UsernamePasswordCredentials(ELASTICSEARCH_USERNAME, ELASTICSEARCH_PASSWORD)
    );

    RestClient client =
        RestClient
            .builder(HttpHost.create("https://" + elasticsearch.getHttpHostAddress()))
            .setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                httpClientBuilder.setSSLContext(elasticsearch.createSslContextFromCa());
                return httpClientBuilder;
            })
            .build();

    Response response = client.performRequest(new Request("GET", "/_cluster/health"));
    assertEquals(200, response.getStatusLine().getStatusCode(), () -> response.toString());
  }

  @Test
  void opensearch() throws IOException {
    RestClient client =
        RestClient
            .builder(HttpHost.create("http://" + opensearch.getHttpHostAddress()))
            .build();

    Response response = client.performRequest(new Request("GET", "/_cluster/health"));
    assertEquals(200, response.getStatusLine().getStatusCode(), () -> response.toString());
  }
}
