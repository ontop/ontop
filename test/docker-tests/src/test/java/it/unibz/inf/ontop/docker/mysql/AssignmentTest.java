package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


/**
 * Class to test if bind in SPARQL is working properly.
 * Some tests check everything is working in combination with CONCAT

 */
public class AssignmentTest extends AbstractVirtualModeTest {

    private static final String owlFile = "/mysql/bindTest/ontologyOdbs.owl";
    private static final String obdaFile = "/mysql/bindTest/mappingsOdbs.obda";
    private static final String propertyFile = "/mysql/bindTest/mappingsOdbs.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertyFile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Test
    public void testBindQuery() throws Exception {
        String queryBind = "PREFIX : <http://myproject.org/odbs#> \n" +
                "\n" +
                "SELECT DISTINCT ?f ?d " +
                " ?price \n" +
                "WHERE {?f a :Film; :hasDirector ?d . \n" +
                "BIND (\"123\" AS ?price) \n" +
                "}";

        countResults(500, queryBind);
    }

    @Test
    public void testBindAndConcatQuery() throws Exception {
        String queryConcat1 = "PREFIX : <http://myproject.org/odbs#> \n" +
                "\n" +
                "SELECT DISTINCT ?f ?d " +
                " ?price \n" +
                "WHERE {?f a :Film; :title ?t; :hasDirector ?d . \n" +
                "BIND (CONCAT(\"123\", \"456\")  as ?price  )    " +
                "}";

        String queryConcat2 = "PREFIX : <http://myproject.org/odbs#> \n" +
                "\n" +
                "SELECT DISTINCT ?f ?d " +
                " ?price \n" +
                "WHERE {?f a :Film; :title ?t; :hasDirector ?d . \n" +
                "BIND (CONCAT(?t, ?t)  as ?price  )    " +
                "}";

//        String queryConcat3 = "PREFIX : <http://myproject.org/odbs#> \n" +
//                "\n" +
//                "SELECT DISTINCT ?f ?d " +
//                " ?price \n" +
//                "WHERE {?f a :Film; :title ?t; :hasDirector ?d . \n" +
//                "BIND (CONCAT(\"123\", \"456\")  as ?price  ) " +
//                "FILTER (REGEX(?price, 6, \"i\"))   " +
//                "}";

        countResults(500, queryConcat2);
    }

    @Test
    public void testSelectQuery() throws Exception {
        String querySelect = "PREFIX : <http://myproject.org/odbs#> \n" +

                "SELECT DISTINCT ?f ?d (\"123\" AS ?price)  \n" +
                "WHERE {?f a :Film; :hasDirector ?d .  \n" +
                "}";

        countResults(500, querySelect);
    }

    @Test
    public void testSelectWithConcatQuery() throws Exception {
        String querySelConcat = "PREFIX : <http://myproject.org/odbs#> \n" +

                "SELECT DISTINCT ?f ?d (CONCAT(\"123\", \"456\") AS ?price)  \n" +
                "WHERE {?f a :Film; :hasDirector ?d .  \n" +
                "}";



        countResults(500, querySelConcat);
    }

    @Test
    public void testConcatWithIntegerQuery() throws Exception {
        String querySelConcat = "PREFIX : <http://myproject.org/odbs#> \n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +

                "SELECT DISTINCT ?f ?d (CONCAT(\"123\"^^xsd:integer, 456) AS ?price)  \n" +
                "WHERE {?f a :Film; :hasDirector ?d .  \n" +
                "}";

        countResults(500, querySelConcat);
    }

}

