package it.unibz.inf.ontop.cli;

import com.google.common.collect.ImmutableMap;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class AbstractOntopEndpointWithAccessControlTest {

    private final String port;

    protected AbstractOntopEndpointWithAccessControlTest(String port) {
        this.port = port;
    }

    @Test
    public void testAsLibrarian() {
        String sparqlEndpoint = "http://localhost:" + port + "/sparql";
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
        String sparqlEndpoint = "http://localhost:" + port + "/sparql";
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
    public void testAsReaderAndLibrarian1() {
        String sparqlEndpoint = "http://localhost:" + port + "/sparql";
        SPARQLRepository repo = new SPARQLRepository(sparqlEndpoint);
        repo.setAdditionalHttpHeaders(
                ImmutableMap.of(
                        "x-user", "joe",
                        "x-roles", "librarian,reader"
                ));
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
    }

    @Test
    public void testAsReaderAndLibrarian2() {
        String sparqlEndpoint = "http://localhost:" + port + "/sparql";
        SPARQLRepository repo = new SPARQLRepository(sparqlEndpoint);
        repo.setAdditionalHttpHeaders(
                ImmutableMap.of(
                        "x-user", "joe",
                        "x-roles", "reader",
                        "x-groups", "librarians"
                ));
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
    }

    @Test
    public void testAsAnonymous() {
        String sparqlEndpoint = "http://localhost:" + port + "/sparql";
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

    @Test
    public void testAsRoger1() {
        String sparqlEndpoint = "http://localhost:" + port + "/sparql";
        SPARQLRepository repo = new SPARQLRepository(sparqlEndpoint);
        repo.setAdditionalHttpHeaders(ImmutableMap.of("x-user", "roger"));
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
                assertEquals(7, count);
            }
        }
    }

    @Test
    public void testAsRoger2() {
        String sparqlEndpoint = "http://localhost:" + port + "/sparql";
        SPARQLRepository repo = new SPARQLRepository(sparqlEndpoint);
        repo.setAdditionalHttpHeaders(ImmutableMap.of("x-user", "roger", "x-roles", "reader,fan"));
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
                assertEquals(7, count);
            }
        }
    }
}
