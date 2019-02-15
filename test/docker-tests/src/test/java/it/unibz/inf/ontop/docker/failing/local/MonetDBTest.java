package it.unibz.inf.ontop.docker.failing.local;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.impl.DefaultOntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;

@Ignore("Local test")
public class MonetDBTest {

        final String owlFile = "/local/monet/booktutorial.owl";
        final String obdaFile = "/local/monet/booktutorial.obda";
        final String propertyFile = "/local/monet/booktutorial.properties";

        @Test
        public void runQuery() throws Exception {

            String owlFileName =  this.getClass().getResource(owlFile).toString();
            String obdaFileName =  this.getClass().getResource(obdaFile).toString();
            String propertyFileName =  this.getClass().getResource(propertyFile).toString();
            /* 
            * Create the instance of Quest OWL reasoner. 
            */
            OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
            OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                    .nativeOntopMappingFile(obdaFileName)
                    .ontologyFile(owlFileName)
                    .propertyFile(propertyFileName)
                    .enableTestMode()
                    .build();
            OntopOWLReasoner reasoner = factory.createReasoner(config);

            /* 
            * Prepare the data connection for querying. 
            */
            OWLConnection conn = reasoner.getConnection();
            OWLStatement st = conn.createStatement();

            /* 
            * Get the book information that is stored in the database 
            */
            String sparqlQuery =
                    "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "select ?x ?y where {?x rdf:type :Author. ?x :name ?y. FILTER regex(?y,\"Carr\")}";
//                    "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                    "select ?title ?author ?genre ?edition where {\n" +
//                    "?x a :Book; :title ?title; :genre ?genre; :writtenBy ?y; :hasEdition ?z.\n" +
//                            "?y a :Author; :name ?author.\n"+
//                        "?z a :Edition; :editionNumber ?edition}";
//                    "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n"+
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
//                    "select ?x ?y ?z where {?x rdf:type :SpecialEdition. ?x :dateOfPublication ?y. ?x :editionNumber ?z}";

            try {
                long t1 = System.currentTimeMillis();
                TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
                while (rs.hasNext()) {
                    final OWLBindingSet bindingSet = rs.next();
                    System.out.print(bindingSet + "\n");
                }
                rs.close();
                long t2 = System.currentTimeMillis();

                /* 
                * Print the query summary 
                */
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
                System.out.println((t2-t1) + "ms");

            } finally {
            /* 
            * Close connection and resources 
            * */
                if (st != null && !st.isClosed()) {
                    st.close();
                }

            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            reasoner.dispose();
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
