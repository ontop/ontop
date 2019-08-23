package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;


/**
 * Class to test if bind in SPARQL is working properly.
 * Some tests check everything is working in combination with CONCAT

 */
public class AssignmentTest extends AbstractVirtualModeTest {

    private static final String owlFile = "/mysql/bindTest/ontologyOdbs.owl";
    private static final String obdaFile = "/mysql/bindTest/mappingsOdbs.obda";
    private static final String propertyFile = "/mysql/bindTest/mappingsOdbs.properties";

    public AssignmentTest() {
        super(owlFile, obdaFile, propertyFile);
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

        countResults(queryBind, 500);
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

        countResults(queryConcat2, 500);
    }

    @Test
    public void testSelectQuery() throws Exception {
        String querySelect = "PREFIX : <http://myproject.org/odbs#> \n" +

                "SELECT DISTINCT ?f ?d (\"123\" AS ?price)  \n" +
                "WHERE {?f a :Film; :hasDirector ?d .  \n" +
                "}";

        countResults(querySelect, 500);
    }

    @Test
    public void testSelectWithConcatQuery() throws Exception {
        String querySelConcat = "PREFIX : <http://myproject.org/odbs#> \n" +

                "SELECT DISTINCT ?f ?d (CONCAT(\"123\", \"456\") AS ?price)  \n" +
                "WHERE {?f a :Film; :hasDirector ?d .  \n" +
                "}";



        countResults(querySelConcat, 500);
    }

    @Test
    public void testConcatWithIntegerQuery() throws Exception {
        String querySelConcat = "PREFIX : <http://myproject.org/odbs#> \n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +

                "SELECT DISTINCT ?f ?d (CONCAT(\"123\"^^xsd:integer, 456) AS ?price)  \n" +
                "WHERE {?f a :Film; :hasDirector ?d .  \n" +
                "}";

        countResults(querySelConcat, 500);
    }

}

