package it.unibz.inf.ontop.cli;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class OntopEndpointDownloadOntologyTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();
    private static String PORT = "29832";
    private static String DBURL = "jdbc:h2:tcp://localhost:19123/./src/test/resources/h2/books;ACCESS_MODE_DATA=r";
    private static String DBUSER = "sa";
    private static String DBPASSWORD = "test";

    @BeforeClass
    public static void setupEndpoint() {
        Ontop.main("endpoint", "-m", "src/test/resources/books/exampleBooks.obda",
                //"-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                //"-d", "src/test/resources/output/exampleBooks-metadata.json",
                //"-v", "src/test/resources/output/exampleBooks-metadata.json",
                "--db-url=" + DBURL,
                //"--db-driver="
                "--db-user=" + DBUSER,
                "--db-password=" + DBPASSWORD,
                "--port=" + PORT,
                "--enable-download-ontology");
    }


    @Test
    public void testOntologyFetcherGET() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:" + PORT + "/ontology");

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat(
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK)); // Should be disabled by default
    }

    @Test
    public void testOntologyFetcherPost() throws IOException {
        HttpUriRequest request = new HttpPost("http://localhost:" + PORT + "/ontology");

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat(
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK)); // The controller should be disabled by default
    }
}
