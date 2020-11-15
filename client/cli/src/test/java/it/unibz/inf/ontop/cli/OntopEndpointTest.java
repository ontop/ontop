package it.unibz.inf.ontop.cli;

import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class OntopEndpointTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();
    private static final String PORT = "29831";

    @BeforeClass
    public static void setupEndpoint() {

        String[] argv = {"endpoint", "-m", "src/test/resources/books/exampleBooks.obda",
            "-p", "src/test/resources/books/exampleBooks.properties",
            "-t", "src/test/resources/books/exampleBooks.owl",
            "--port=" + PORT};
        Ontop.main(argv);
    }

    @Test
    public void testQuery() {

        String sparqlEndpoint = "http://localhost:" + PORT + "/sparql";
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.init();

        try (RepositoryConnection conn = repo.getConnection()) {
            String queryString = "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n"
                    + "SELECT DISTINCT ?x ?title ?author ?genre ?edition\n"
                    + "WHERE { ?x a :Book; :title ?title; :genre ?genre; :writtenBy ?y; :hasEdition ?z.\n"
                    + "\t\t ?y a :Author; :name ?author.\n"
                    + "\t\t ?z a :Edition; :editionNumber ?edition\n"
                    + "}";
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

    @Test(expected = QueryEvaluationException.class)
    public void testInvalidQuery() {

        String sparqlEndpoint = "http://localhost:" + PORT + "/sparql";
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.init();
        try (RepositoryConnection conn = repo.getConnection()) {
            String queryString = "X";
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                assertTrue(result.hasNext());
            }
        }
    }

    @Test
    public void testFederatedQuery() {

        String sparqlEndpoint = "http://localhost:" + PORT + "/sparql";
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.init();

        try (RepositoryConnection conn = repo.getConnection()) {
            String queryString = "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n"
                    + "SELECT DISTINCT ?x\n"
                    + "WHERE {"
                     + "SERVICE <"+sparqlEndpoint+"> {"
                     + "?x :title \"How to get fired\"."
                    + "}}";
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                assertTrue(result.hasNext());
                BindingSet bindingSet = result.next();
                final Binding binding = bindingSet.getBinding("x");
                assertNotNull(binding);
                assertEquals("Expect book:11","http://meraka/moss/exampleBooks.owl#book/11/", binding.getValue().stringValue());
                assertFalse(result.hasNext());
            }
        }
    }
}
