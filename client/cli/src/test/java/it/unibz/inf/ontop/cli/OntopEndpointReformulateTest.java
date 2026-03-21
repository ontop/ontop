package it.unibz.inf.ontop.cli;

import com.google.common.collect.Lists;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * Tests for the /ontop/reformulate endpoint, available in dev mode.
 * Tests reformulation with and without the forNativeConsumption parameter.
 */
public class OntopEndpointReformulateTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();
    private static final String PORT = "29835";
    private static final String DBURL = "jdbc:h2:tcp://localhost:19123/./src/test/resources/h2/books;ACCESS_MODE_DATA=r";
    private static final String DBUSER = "sa";
    private static final String DBPASSWORD = "test";
    private static final Logger LOGGER = LoggerFactory.getLogger(OntopEndpointReformulateTest.class);

    private static final String SELECT_QUERY = "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
            "SELECT ?x ?title WHERE { ?x a :Book; :title ?title }";

    private static final String SELECT_MULTI_TYPED_QUERY = "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
            "SELECT ?x ?v WHERE { ?x a :Book . \n" +
            " { ?x :title ?v } UNION { BIND (2 AS ?v) } }";

    private static final String SELECT_QUERY_2 = "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
            "SELECT DISTINCT ?x ?title ?author ?genre ?edition\n" +
            "WHERE { ?x a :Book; :title ?title; :genre ?genre; :writtenBy ?y.\n" +
            "\t\t ?y a :Author; :name ?author.\n" +
            "\t\t ?x :hasEdition ?z.\n" +
            "\t\t ?z a :Edition; :editionNumber ?edition\n" +
            "}";

    private static final String CONSTRUCT_QUERY = "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
            "CONSTRUCT { ?x :title ?title } WHERE { ?x a :Book; :title ?title }";

    private static final String ASK_QUERY = "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
            "ASK { ?x a :Book; :title ?title }";

    @BeforeClass
    public static void setupEndpoint() {
        Ontop.main("endpoint", "-m", "src/test/resources/books/exampleBooks.obda",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "--db-url=" + DBURL,
                "--db-user=" + DBUSER,
                "--db-password=" + DBPASSWORD,
                "--port=" + PORT,
                "--dev");
    }

    @Test
    public void testReformulateSelectDefault() throws IOException {
        String body = reformulate(SELECT_QUERY);
        LOGGER.debug("Reformulation SELECT (default):\n{}", body);
        assertFalse("Reformulation should not be empty", body.trim().isEmpty());
    }

    @Test
    public void testReformulateSelectNotForNativeConsumption() throws IOException {
        String body = reformulate(SELECT_QUERY, false);
        LOGGER.debug("Reformulation SELECT (not for native consumption):\n{}", body);
        assertFalse("Reformulation should not be empty", body.trim().isEmpty());
    }

    @Test
    public void testReformulateSelectForNativeConsumption() throws IOException {
        String query = reformulate(SELECT_QUERY, true);
        LOGGER.debug("Reformulation SELECT (for native consumption):\n{}", query);
        testColumnOrder(query, List.of("x", "title"));
        assertTrue("Native consumption body should only contain the SQL query", query.trim().startsWith("SELECT"));
    }

    @Test
    public void testReformulateMultiTypedSelectNotNative() throws IOException {
        String body = reformulate(SELECT_MULTI_TYPED_QUERY, false);
        LOGGER.debug("Reformulation SELECT (default):\n{}", body);
        assertFalse("Reformulation should not be empty", body.trim().isEmpty());
    }

    @Test(expected = FailRequestException.class)
    public void testReformulateMultiTypedSelectForNativeConsumption() throws IOException {
        var query = reformulate(SELECT_MULTI_TYPED_QUERY, true);
        fail("Reformulation should fail for multi-typed query when forNativeConsumption is true. Query: " + query);
    }

    @Test
    public void testReformulateSelectJoinDefault() throws IOException {
        String body = reformulate(SELECT_QUERY_2);
        LOGGER.debug("Reformulation SELECT JOIN (default):\n{}", body);
        assertFalse("Reformulation should not be empty", body.trim().isEmpty());
    }

    @Test
    public void testReformulateSelectJoinForNativeConsumption() throws IOException {
        String query = reformulate(SELECT_QUERY_2, true);
        LOGGER.debug("Reformulation SELECT JOIN (for native consumption):\n{}", query);
        testColumnOrder(query, List.of("x", "title", "author", "genre", "edition"));
        assertTrue("Native consumption body should only contain the SQL query", query.trim().startsWith("SELECT"));
    }

    private void testColumnOrder(String query, List<String> expectedColumns) {
        // Verify that the SQL projects columns in the same order as the SPARQL SELECT clause
        Matcher matcher = Pattern.compile("AS \"(\\w+)\"")
                .matcher(query.substring(0, query.indexOf("FROM")));
        List<String> projectedColumns = Lists.newArrayList();
        while (matcher.find()) {
            projectedColumns.add(matcher.group(1));
        }
        assertEquals("Projected columns should follow SPARQL SELECT order",
                expectedColumns, projectedColumns);
    }

    @Test
    public void testReformulateConstructDefault() throws IOException {
        String body = reformulate(CONSTRUCT_QUERY);
        LOGGER.debug("Reformulation CONSTRUCT (default):\n{}", body);
        assertFalse("Reformulation should not be empty", body.trim().isEmpty());
    }

    @Test
    public void testReformulateConstructForNativeConsumption() throws IOException {
        String body = reformulate(CONSTRUCT_QUERY, true);
        LOGGER.debug("Reformulation CONSTRUCT (for native consumption):\n{}", body);
        assertTrue("Native consumption body should only contain the SQL query", body.trim().startsWith("SELECT"));
    }

    @Test
    public void testReformulateAskDefault() throws IOException {
        String body = reformulate(ASK_QUERY);
        LOGGER.debug("Reformulation ASK (default):\n{}", body);
        assertFalse("Reformulation should not be empty", body.trim().isEmpty());
    }

    @Test
    public void testReformulateAskForNativeConsumption() throws IOException {
        String body = reformulate(ASK_QUERY, true);
        LOGGER.debug("Reformulation ASK (for native consumption):\n{}", body);
        assertTrue("Native consumption body should only contain the SQL query", body.trim().startsWith("SELECT"));
    }

    private String reformulate(String sparqlQuery) throws IOException {
        String encodedQuery = URLEncoder.encode(sparqlQuery, StandardCharsets.UTF_8);
        String url = "http://localhost:" + PORT + "/ontop/reformulate?query=" + encodedQuery;
        HttpUriRequest request = new HttpGet(url);
        HttpResponse response = HttpClientBuilder.create().build().execute(request);
        assertThat("Reformulate endpoint should return 200",
                response.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));
        return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    }

    private String reformulate(String sparqlQuery, boolean forNativeConsumption) throws IOException, FailRequestException {
        String encodedQuery = URLEncoder.encode(sparqlQuery, StandardCharsets.UTF_8);
        String url = "http://localhost:" + PORT + "/ontop/reformulate?query=" + encodedQuery
                + "&forNativeConsumption=" + forNativeConsumption;
        HttpUriRequest request = new HttpGet(url);
        HttpResponse response = HttpClientBuilder.create().build().execute(request);
        var statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= 400)
            throw new FailRequestException();

        assertThat("Reformulate endpoint should return 200",
                statusCode,
                equalTo(HttpStatus.SC_OK));
        return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    }

    private static class FailRequestException extends RuntimeException {
    }
}
