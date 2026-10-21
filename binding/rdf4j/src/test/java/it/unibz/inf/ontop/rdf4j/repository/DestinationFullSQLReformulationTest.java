package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class DestinationFullSQLReformulationTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/destination/dest.obda";
    private static final String SQL_SCRIPT = "/destination/schema.sql";
    private static final String ONTOLOGY_FILE = "/destination/dest.owl";
    private static final String PROPERTIES_FILE = "/destination/dest-full-sql.properties";

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
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                "\n" +
                "SELECT ?h ?posLabel ?posColor\n" +
                "WHERE {\n" +
                "  ?h a schema:LodgingBusiness ;\n" +
                "     schema:name ?posLabel .\n" +
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
                "LIMIT 500\n";

        String sql = reformulateIntoNativeQuery(sparql, true);
        int count = runQueryAndCount(sparql);

        assertTrue(sql.toUpperCase().contains("UNION ALL"));
        // TODO: feel free to update it according to changes in the SQL generation.
        //  It must preserve the variable order as in the SPARQL query.
        assertTrue(sql.startsWith("SELECT V9.\"h\" AS \"h\", V9.\"posLabel\" AS \"posLabel\", V9.\"posColor\" AS \"posColor\""));
        assertEquals(1, count);
    }

    /**
     * SPARQL query rejected because it is not strongly typed
     */
    @Test(expected = RepositoryException.class)
    public void testSPO() {
        reformulateIntoNativeQuery(
                "SELECT * WHERE {\n" +
                        "  ?s ?p ?o \n" +
                        "}\n" +
                        "LIMIT 100000",
                true);
    }

    @Test
    public void testSPOWithFilter() {
        reformulateIntoNativeQuery(
                "SELECT * WHERE {\n" +
                        "  ?s ?p ?o \n" +
                        "  FILTER (datatype(?o) = <http://www.w3.org/2001/XMLSchema#string>)" +
                        "}\n" +
                        "LIMIT 10",
                true);
    }

    @Test
    public void testName() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX : <http://noi.example.org/ontology/odh#>\n" +
                "\n" +
                "SELECT ?h ?v\n" +
                "WHERE {\n" +
                "  ?h a schema:LodgingBusiness ;\n" +
                "     schema:name ?v .\n" +
                "  FILTER (lang(?v) = 'de')\n" +
                "}\n" +
                "LIMIT 1\n";

        String sql = reformulateIntoNativeQuery(sparql, true);
        runQueryAndCompare(sparql, ImmutableSet.of("eee"));

        assertTrue(sql.toUpperCase().contains("UNION ALL"));
    }

    /**
     * Regression for <a href="https://github.com/ontop/ontop/issues/956">#956</a>:
     * a FILTER that is statically false (schema:name is langString, not xsd:integer) must
     * reformulate to empty SQL for native consumption, not throw
     * NotFullyTranslatableToNativeQueryException.
     */
    @Test
    public void testEmptyQueryStaticallyFalseDatatypeFilter() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT DISTINCT ?focus_node ?value\n" +
                "WHERE {\n" +
                "  ?focus_node schema:name ?value .\n" +
                "  FILTER (!isLiteral(?value) || datatype(?value) = xsd:integer)\n" +
                "}\n";

        String sql = reformulateIntoNativeQuery(sparql, true);
        assertNotNull(sql);
        assertFalse(sql.isEmpty());
        assertTrue(sql.contains("focus_node"));
        assertTrue(sql.contains("value"));
        assertTrue(sql.indexOf("focus_node") < sql.indexOf("value"));
        assertEquals(0, runQueryAndCount(sparql));
    }

    /**
     * Regression for <a href="https://github.com/ontop/ontop/issues/956">#956</a>:
     * an unmapped predicate yields EmptyNode at unfold time; native reformulation must
     * still emit empty SQL.
     */
    @Test
    public void testEmptyQueryUnmappedPredicate() {
        String sparql = "PREFIX : <http://destination.example.org/ontology/dest#>\n" +
                "SELECT ?focus_node ?value\n" +
                "WHERE {\n" +
                "  ?focus_node :unmappedPredicate ?value .\n" +
                "}\n";

        String sql = reformulateIntoNativeQuery(sparql, true);
        assertNotNull(sql);
        assertFalse(sql.isEmpty());
        assertTrue(sql.contains("focus_node"));
        assertTrue(sql.contains("value"));
        assertTrue(sql.indexOf("focus_node") < sql.indexOf("value"));
        assertEquals(0, runQueryAndCount(sparql));
    }

}
