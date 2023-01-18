package it.unibz.inf.ontop.docker.mysql;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

public class DatetimestampObdaTest extends AbstractVirtualModeTest  {

    private static final String owlFile = "/mysql/northwind/northwind-dmo.owl";
    private static final String obdaFile = "/mysql/northwind/mapping-northwind-dmo.obda";
    private static final String propertyFile = "/mysql/northwind/mapping-northwind-dmo.properties";

    private static EngineConnection CONNECTION;

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlFile, obdaFile, propertyFile);
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
    }

    @Test
    public void testRequiredDate() throws Exception {
        countResults(830, "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Orders/>\n" +
                "select *\n" +
                "{?x :RequiredDate ?y}");
    }

    @Test
    public void testShippedDate() throws Exception {
        countResults(809, "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Orders/>\n" +
                "select *\n" +
                " {?x :ShippedDate ?y}");
    }

    @Test
    public void testHireDate() throws Exception {
        countResults(9, "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Employees/>\n" +
                "select *\n" +
                "{?x :HireDate ?y}");
    }

    @Test
    public void testBirthDate() throws Exception {
        countResults(9, "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Employees/>\n" +
                "select *\n" +
                "{?x :BirthDate ?y}");
    }

    @Test
    public void testPicture() throws Exception {
        countResults(8, "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Categories/>\n" +
                "select *\n" +
                "{?x :Picture ?y}");
    }

    @Test
    public void testPhoto() throws Exception {
        countResults(9, "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Employees/>\n" +
                "select *\n" +
                "{?x :Photo ?y}");
    }
}

