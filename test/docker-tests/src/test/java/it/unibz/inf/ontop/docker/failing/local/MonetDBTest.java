package it.unibz.inf.ontop.docker.failing.local;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;

import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.impl.DefaultOntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Local test")
public class MonetDBTest {

    private static final String owlFile = "/local/monet/booktutorial.owl";
    private static final String obdaFile = "/local/monet/booktutorial.obda";
    private static final String propertyFile = "/local/monet/booktutorial.properties";

    @Test
    public void runQuery() throws Exception {

        String owlFileName =  this.getClass().getResource(owlFile).toString();
        String obdaFileName =  this.getClass().getResource(obdaFile).toString();
        String propertyFileName =  this.getClass().getResource(propertyFile).toString();
        /*
         * Create the instance of Quest OWL reasoner.
         */
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFileName)
                .ontologyFile(owlFileName)
                .propertyFile(propertyFileName)
                .enableTestMode()
                .build();


        /*
         * Get the book information that is stored in the database
         */
        //                    "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                    "select ?title ?author ?genre ?edition where {\n" +
//                    "?x a :Book; :title ?title; :genre ?genre; :writtenBy ?y; :hasEdition ?z.\n" +
//                            "?y a :Author; :name ?author.\n"+
//                        "?z a :Edition; :editionNumber ?edition}";
//                    "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n"+
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
//                    "select ?x ?y ?z where {?x rdf:type :SpecialEdition. ?x :dateOfPublication ?y. ?x :editionNumber ?z}";

        try (OntopOWLEngine reasoner = new SimpleOntopOWLEngine(config);
             OWLConnection conn = reasoner.getConnection();
             OWLStatement st = conn.createStatement()) {
            String sparqlQuery =
                    "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                            "select ?x ?y where {?x rdf:type :Author. ?x :name ?y. FILTER regex(?y,\"Carr\")}";
            long t1 = System.currentTimeMillis();
            TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                System.out.print(bindingSet + "\n");
            }
            rs.close();
            long t2 = System.currentTimeMillis();

            /* Print the query summary*/
            DefaultOntopOWLStatement qst = (DefaultOntopOWLStatement) st;
            String sqlQuery = qst.getRewritingRendering(sparqlQuery);
            System.out.println();
            System.out.println("The input SPARQL query:");
            System.out.println("=======================");
            System.out.println(sparqlQuery);
            System.out.println();
            System.out.println("The output SQL query:");
            System.out.println("=====================");
            System.out.println(sqlQuery);
            System.out.println("Query Execution Time:");
            System.out.println("=====================");
            System.out.println((t2 - t1) + "ms");

        }
    }
    /**
     * Main client program
     * */
    public static void main(String[] args) {
        try {
            MonetDBTest example = new MonetDBTest();
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
