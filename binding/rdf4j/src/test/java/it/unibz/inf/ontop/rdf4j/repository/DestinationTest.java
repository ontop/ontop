package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class DestinationTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/destination/dest.obda";
    private static final String SQL_SCRIPT = "/destination/schema.sql";
    private static final String ONTOLOGY_FILE = "/destination/dest.owl";
    private static final String PROPERTIES_FILE = "/destination/dest.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, ONTOLOGY_FILE, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testQuery() {
        int count = runQueryAndCount("PREFIX schema: <http://schema.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                "\n" +
                "SELECT ?h ?posLabel ?posColor\n" +
                "WHERE {\n" +
                "  ?h a schema:LodgingBusiness ;\n" +
                "     schema:name ?posLabel .\n" +
                "  #?h schema:containedInPlace/schema:name \"Bozen\"@de . # Uncomment for restricting to a municipality\n" +
                "  FILTER (lang(?posLabel) = 'de')\n" +
                "  \n" +
                "    # Colors\n" +
                "  OPTIONAL {\n" +
                "    ?h a schema:Campground .\n" +
                "    BIND(\"chlorophyll,0.5\" AS ?posColor) # Green\n" +
                "  }\n" +
                "    OPTIONAL {\n" +
                "    ?h a schema:BedAndBreakfast .\n" +
                "    BIND(\"viridis,0.1\" AS ?posColor) # Purple\n" +
                "  }\n" +
                "  OPTIONAL {\n" +
                "    ?h a schema:Hotel . \n" +
                "    BIND(\"jet,0.3\" AS ?posColor) # Light blue\n" +
                "  }\n" +
                "  OPTIONAL {\n" +
                "    ?h a schema:Hostel .\n" +
                "    BIND(\"jet,0.8\" AS ?posColor) # Red\n" +
                "  }\n" +
                "\n" +
                "}\n" +
                "LIMIT 500\n");
        assertEquals(1, count);
    }

    @Test
    public void testSubQueryOrderByNonProjectedVariable() {
        int count = runQueryAndCount("PREFIX schema: <http://schema.org/>\n" +
                "\n" +
                "SELECT * WHERE {\n" +
                "  { SELECT DISTINCT ?h ?nStr WHERE {\n" +
                "      ?h a schema:LodgingBusiness ;\n" +
                "         schema:name ?n .\n" +
                "      BIND(str(?n) AS ?nStr)\n" +
                "    }\n" +
                "    ORDER BY DESC(CONCAT(?nStr, ?nStr))\n" +
                "    LIMIT 2\n" +
                "  }\n" +
                "  ?h schema:name ?name\n" +
                "}");

        assertEquals(6, count);
    }

    @Test
    public void testSPO() {
        runQueryAndCount(
                "SELECT * WHERE {\n" +
                        "  ?s ?p ?o \n" +
                        "  VALUES ?p {\n" +
                        "<http://qudt.org/schema/qudt#conversionOffset>\n" +
                        "<http://www.linkedmodel.org/schema/vaem#namespace>\n" +
                        "  }" +
                        "}\n" +
                        "LIMIT 10");
    }

}
