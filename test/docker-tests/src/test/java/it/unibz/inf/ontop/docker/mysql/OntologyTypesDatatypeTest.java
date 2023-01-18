package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import static org.junit.Assert.assertEquals;

/**
 * Test if the datatypes xsd:date, xsd:time and xsd:year are returned correctly.
 *
 */

public class OntologyTypesDatatypeTest extends AbstractVirtualModeTest {

    private static final String owlfile = "/testcases-docker/datetime/datatypes.owl";
    private static final String obdafile = "/testcases-docker/datetime/datatypes-mysql.obda";
    private static final String propertiesfile = "/testcases-docker/datetime/datatypes-mysql.properties";

    private static EngineConnection CONNECTION;

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlfile, obdafile, propertiesfile);
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
    }


    //With QuestOWL the results for xsd:date, xsd:time and xsd:year are returned as a plain literal since OWLAPI3 supports only xsd:dateTime
	@Test
    public void testDatatypeDate() throws Exception {

        String query1 = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?s ?x\n" +
                "WHERE {\n" +
                "   ?s a :Row; :hasDate ?x\n" +
                "   FILTER ( ?x = \"2013-03-18\"^^xsd:date ) .\n" +
                "}";

        String result = runQueryAndReturnStringOfLiteralX(query1);
		assertEquals("\"2013-03-18\"^^xsd:date",result );
	}

    @Test
    public void testDatatypeTime() throws Exception {

        String query1 = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?s ?x\n" +
                "WHERE {\n" +
                "   ?s a :Row; :hasTime ?x\n" +
                "   FILTER ( ?x = \"10:12:10\"^^xsd:time ) .\n" +
                "}";

        String result = runQueryAndReturnStringOfLiteralX(query1);
        assertEquals("\"10:12:10\"^^xsd:time", result );
    }

    @Ignore
    public void testDatatypeTimeTz() throws Exception {

        String query1 = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?s ?x\n" +
                "WHERE {\n" +
                "   ?s a :Row; :hasTime ?x\n" +
                "   FILTER ( ?x = \"10:12:10+01:00\"^^xsd:time ) .\n" +
                "}";

        String result = runQueryAndReturnStringOfLiteralX(query1);
        assertEquals("\"10:12:10+01:00\"^^xsd:time", result );
    }

    @Test
    public void testDatatypeYear() throws Exception {
        String query1 = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?s ?x\n" +
                "WHERE {\n" +
                "   ?s a :Row; :hasYear ?x\n" +
                "   FILTER ( ?x = \"2013\"^^xsd:gYear ) .\n" +
                "}";
        String result = runQueryAndReturnStringOfLiteralX(query1);
        assertEquals("\"2013\"^^xsd:gYear",result );
    }



}
