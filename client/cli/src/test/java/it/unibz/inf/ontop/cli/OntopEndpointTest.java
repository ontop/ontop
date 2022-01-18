package it.unibz.inf.ontop.cli;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class OntopEndpointTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();
    private static String PORT = "29831";
    private static String DBNAME = "books";
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
                "--port=" + PORT);
    }


    @Test
    public void testQuery() {
        String sparqlEndpoint = "http://localhost:" + PORT + "/sparql";
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.initialize();

        try (RepositoryConnection conn = repo.getConnection()) {
            String queryString = "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                    "SELECT DISTINCT ?x ?title ?author ?genre ?edition\n" +
                    "WHERE { ?x a :Book; :title ?title; :genre ?genre; :writtenBy ?y; :hasEdition ?z.\n" +
                    "\t\t ?y a :Author; :name ?author.\n" +
                    "\t\t ?z a :Edition; :editionNumber ?edition\n" +
                    "}";

            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            //TupleQueryResult result = tupleQuery.evaluate();
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {  // iterate over the result
                    BindingSet bindingSet = result.next();
                    //Value movie = bindingSet.getValue("teacher");
                    System.out.println(bindingSet);
                }
            }
        }
    }

    @Test
    public void testDescribeQuery() {
        String sparqlEndpoint = "http://localhost:" + PORT + "/sparql";
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.initialize();

        try (RepositoryConnection conn = repo.getConnection()) {
            String queryString = "DESCRIBE <http://meraka/moss/exampleBooks.owl#book/10/>";
            GraphQuery graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
            //TupleQueryResult result = tupleQuery.evaluate();
            try (GraphQueryResult result = graphQuery.evaluate()) {
                while (result.hasNext()) {  // iterate over the result
                    Statement bindingSet = result.next();
                    //Value movie = bindingSet.getValue("teacher");
                    System.out.println(bindingSet);
                }
            }
        }
    }
    
    @Test(expected = QueryEvaluationException.class)
    public void testInvalidQuery() {

        String sparqlEndpoint = "http://localhost:" + PORT + "/sparql";
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.initialize();
        try (RepositoryConnection conn = repo.getConnection()) {
            String queryString = "X";
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result = tupleQuery.evaluate();
        }
    }
    
    @Test
    public void testPortal() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:" + PORT + "/");

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat(
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));
    }

    @Test
    public void testOntologyFetcher() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:" + PORT + "/ontology");

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat(
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_NOT_FOUND)); // Should be disabled by default
    }

    @Test
    public void testOntologyFetcherPost() throws IOException {
        HttpUriRequest request = new HttpPost("http://localhost:" + PORT + "/ontology");

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat(
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_NOT_FOUND)); // The controller should be disabled by default
    }

}
