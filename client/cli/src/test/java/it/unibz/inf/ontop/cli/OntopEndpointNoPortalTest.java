package it.unibz.inf.ontop.cli;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class OntopEndpointNoPortalTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();
    private static final String PORT = "29839";
    private static final Logger LOGGER = LoggerFactory.getLogger(OntopEndpointNoPortalTest.class);


    @BeforeClass
    public static void setupEndpoint() {
        Ontop.main("endpoint", "-m", "src/test/resources/books/exampleBooks.obda",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-d", "src/test/resources/exampleBooks-metadata.json",
                "--port=" + PORT,
                "--disable-portal-page");
    }


    @Test
    public void testQuery() {

        String sparqlEndpoint = "http://localhost:" + PORT + "/sparql";
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.init();

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
                    LOGGER.debug(bindingSet.toString());
                }
            }
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
                equalTo(HttpStatus.SC_NOT_FOUND));
    }


}
