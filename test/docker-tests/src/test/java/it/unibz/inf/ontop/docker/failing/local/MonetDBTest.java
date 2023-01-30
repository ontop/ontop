package it.unibz.inf.ontop.docker.failing.local;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.connection.impl.DefaultOntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Local test")
public class MonetDBTest extends AbstractVirtualModeTest {

    private static final String owlFile = "/local/monet/booktutorial.owl";
    private static final String obdaFile = "/local/monet/booktutorial.obda";
    private static final String propertyFile = "/local/monet/booktutorial.properties";

    @Test
    public void runQuery() throws Exception {
        //                    "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                    "select ?title ?author ?genre ?edition where {\n" +
//                    "?x a :Book; :title ?title; :genre ?genre; :writtenBy ?y; :hasEdition ?z.\n" +
//                            "?y a :Author; :name ?author.\n"+
//                        "?z a :Edition; :editionNumber ?edition}";
//                    "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n"+
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
//                    "select ?x ?y ?z where {?x rdf:type :SpecialEdition. ?x :dateOfPublication ?y. ?x :editionNumber ?z}";

        try (EngineConnection connection = createReasoner(owlFile, obdaFile, propertyFile);
             OWLStatement st = connection.createStatement()) {
            String sparqlQuery =
                    "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                            "select ?x ?y where {?x rdf:type :Author. ?x :name ?y. FILTER regex(?y,\"Carr\")}";
            long t1 = System.currentTimeMillis();
            try (TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery)) {
                while (rs.hasNext()) {
                    OWLBindingSet bindingSet = rs.next();
                    System.out.print(bindingSet + "\n");
                }
            }
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

    @Override
    protected OntopOWLStatement createStatement()  {
        throw new IllegalStateException("should never be called");
    }
}
