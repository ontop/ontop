package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRISafeConstraintsTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/iri-safe-constraints/mappings.obda";
    private static final String ONTOLOGY_FILE = "/iri-safe-constraints/ontology.ttl";
    private static final String LENSES_FILE = "/iri-safe-constraints/lenses.json";
    private static final String DATABASE_FILE = "/iri-safe-constraints/database.sql";

    @BeforeClass
    public static void before() throws Exception {
        initOBDA(DATABASE_FILE, OBDA_FILE, ONTOLOGY_FILE, null, LENSES_FILE);
    }

    @AfterClass
    public static void after() throws Exception {
        release();
    }

    @Test
    public void testIRISafeWithOptional() {
        // The error only arise when the ?building variable is projected
        String query = "PREFIX ex: <http://example.org/ontology#>\n" +
                "SELECT ?v ?building\n" +
                "{\n" +
                "    ?v a ex:Address .\n" +
                "    OPTIONAL { ?building ex:hasAddress ?v }\n" +
                "}";

        runQueryAndCompare(query, ImmutableSet.of("http://example.org/data/address/Bolzano/Via%20Verdi/1"));
    }
}
