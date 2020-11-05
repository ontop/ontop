package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.*;


public abstract class AbstractRDF4JBnodeProfTest extends AbstractRDF4JTest {

    @Test
    public void testProfessor1() {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  *\n" +
                "WHERE {\n" +
                "  ?v a :Professor ; :firstName ?n \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(8, count);
    }

    @Test
    public void testProfessor2() {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT (COALESCE(str(?p), \"not-available\") AS ?v)\n" +
                "WHERE {\n" +
                "  ?p a :Professor \n" +
                "}" +
                "LIMIT 2";
        // returning the internal label of a bnode could result into a privacy breach
        runQueryAndCompare(query, ImmutableList.of("not-available", "not-available"));
    }

    @Test
    public void testProfessor3() {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  *\n" +
                "WHERE {\n" +
                "  ?v a :Professor ; :firstName \"Michael\" . \n" +
                "}";
        ImmutableList<String> results = runQuery(query);
        assertEquals(results.size(), 1);
        // Makes sure the internal bnode label is not leaked
        assertFalse(results.get(0).contains("professor_40"));
    }

    /**
     * The constant bnode in the SPARQL query should be treated as a variable
     */
    @Test
    public void testProfessor4() {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  *\n" +
                "WHERE {\n" +
                "  _:professor_40 a :Professor ; :firstName ?v \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(8, count);
    }

}
