package it.unibz.inf.ontop.cli;

import com.google.common.collect.ImmutableMap;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import static org.junit.Assert.assertEquals;

public class OntopEndpointWithAccessControlInMappingTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();
    private static final String PORT = "29889";
    private static final String DBURL = "jdbc:h2:tcp://localhost:19123/./src/test/resources/h2/books.h2;ACCESS_MODE_DATA=r";
    private static final String DBUSER = "sa";
    private static final String DBPASSWORD = "test";


    @BeforeClass
    public static void setupEndpoint() {
        Ontop.main("endpoint", "-m", "src/test/resources/books/exampleBooks-access-control.obda",
                "-p", "src/test/resources/books/exampleBooks-access-control.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "--db-url=" + DBURL,
                "--db-user=" + DBUSER,
                "--db-password=" + DBPASSWORD,
                "--port=" + PORT);
    }


    @Test
    public void testAsLibrarian() {
        String sparqlEndpoint = "http://localhost:" + PORT + "/sparql";
        SPARQLRepository repo = new SPARQLRepository(sparqlEndpoint);
        repo.setAdditionalHttpHeaders(ImmutableMap.of("x-roles", "librarian"));
        repo.init();

        String queryString = "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                "SELECT ?x ?edition\n" +
                "WHERE { ?x a :Book; :hasEdition ?z.\n" +
                "\t\t ?z a :SpecialEdition; :editionNumber ?edition\n" +
                "}";

        try (RepositoryConnection conn = repo.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                long count = result.stream().count();
                assertEquals(7, count);
            }
        }

        try (RepositoryConnection conn = repo.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                result.stream().count();
            }
        }
    }

    @Test
    public void testAsReader() {
        String sparqlEndpoint = "http://localhost:" + PORT + "/sparql";
        SPARQLRepository repo = new SPARQLRepository(sparqlEndpoint);
        repo.setAdditionalHttpHeaders(ImmutableMap.of("x-roles", "reader"));
        repo.init();

        try (RepositoryConnection conn = repo.getConnection()) {
            String queryString = "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                    "SELECT ?x ?edition\n" +
                    "WHERE { ?x a :Book; :hasEdition ?z.\n" +
                    "\t\t ?z a :SpecialEdition; :editionNumber ?edition\n" +
                    "}";

            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                long count = result.stream().count();
                assertEquals(0, count);
            }
        }
    }

    @Test
    public void testAsAnonymous() {
        String sparqlEndpoint = "http://localhost:" + PORT + "/sparql";
        SPARQLRepository repo = new SPARQLRepository(sparqlEndpoint);
        repo.init();

        try (RepositoryConnection conn = repo.getConnection()) {
            String queryString = "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                    "SELECT ?x ?edition\n" +
                    "WHERE { ?x a :Book; :hasEdition ?z.\n" +
                    "\t\t ?z a :SpecialEdition; :editionNumber ?edition\n" +
                    "}";

            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                long count = result.stream().count();
                assertEquals(0, count);
            }
        }
    }

}
