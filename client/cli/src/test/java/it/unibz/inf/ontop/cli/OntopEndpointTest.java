package it.unibz.inf.ontop.cli;

import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class OntopEndpointTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();
    private static String PORT = "29831";


    @BeforeClass
    public static void setupEndpoint() {
        Ontop.main("endpoint", "-m", "src/test/resources/books/exampleBooks.obda",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-d", "src/test/resources/output/exampleBooks-metadata.json",
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


}
