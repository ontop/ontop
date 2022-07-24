package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class MultitypedFactsTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/empty.obda";
    private static final String SQL_SCRIPT = "/destination/schema.sql";
    private static final String ONTOLOGY_FILE = "/multityped-facts.ttl";
    private static final String PROPERTIES_FILE = "/destination/dest-no-tbox.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, ONTOLOGY_FILE, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testCount() {
        int count = runQueryAndCount("SELECT * { ?s ?p ?o }");
        assertEquals(13, count);
    }

    @Test
    public void testObject() {
        runQueryAndCompare("SELECT * { ?s ?p ?v }", ImmutableSet.of("20", "ciao", "true", "plop", "5", "10.0",
                "false", "6", "30", "8.0", "hallo"));
    }

    @Test
    public void testDatatypes() {
        runQueryAndCompare("SELECT DISTINCT ?v { ?s ?p ?o . BIND (datatype(?o) AS ?v) }",
                ImmutableSet.of("http://www.w3.org/2001/XMLSchema#integer",
                        "http://www.w3.org/2001/XMLSchema#decimal",
                        "http://www.w3.org/2001/XMLSchema#string",
                        "http://www.w3.org/2001/XMLSchema#boolean",
                        "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"));
    }

    @Test
    public void testP3() {
        runQueryAndCompare("SELECT * { ?s <http://example.org/p3> ?v }", ImmutableSet.of("false", "true"));
    }

}
