package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class PersonGeneratedMappingTest extends AbstractRDF4JTest {

    private static final String R2RML_FILE = "/person/view-less-generated-mapping.ttl";
    private static final String SQL_SCRIPT = "/person/person.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initR2RML(SQL_SCRIPT, R2RML_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }


    @Test
    public void testSoftwareDeveloper() {
        int count = runQueryAndCount("prefix voc: <http://myvoc.example.org/>\n" +
                "\n" +
                "select * where {\n" +
                "  ?p a voc:SoftwareDeveloper" +
                "}");
        assertEquals(1, count);
    }

    @Test
    public void testProjectManager() {
        int count = runQueryAndCount("prefix voc: <http://myvoc.example.org/>\n" +
                "\n" +
                "select * where {\n" +
                "  ?p a voc:ProjectManager " +
                "}");
        assertEquals(0, count);
    }

    @Test
    public void testCountries() {
        int count = runQueryAndCount("prefix voc: <http://myvoc.example.org/>\n" +
                "\n" +
                "select * where {\n" +
                "  ?p voc:countryOfOrigin ?c " +
                "}");
        assertEquals(1, count);
    }



}
