package it.unibz.inf.ontop.rdf4j.repository;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Reproduces <a href="https://github.com/ontop/ontop/issues/931">issue #931</a>:
 * OPTIONAL with xsd:string typed literal in mapping causes
 * "Was expecting a unique and known DB term type" error.
 *
 * <p>The mapping uses ST_AsGeoJSON to convert geometry to a GeoJSON string,
 * then casts it to xsd:string. When this property is used inside an OPTIONAL
 * clause, Ontop fails to generate a valid LEFT JOIN.</p>
 */
public class Issue931Test {

    private static final String OBDA_FILE = "/issue931/issue931.obda";
    private static final String OWL_FILE = "/issue931/issue931.owl";
    private static final String PROPERTIES_FILE = "/issue931/issue931.properties";

    private static OntopRepositoryConnection CONNECTION;

    @BeforeClass
    public static void before() throws IOException, SQLException {
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(Issue931Test.class.getResource(OBDA_FILE).getPath())
                .ontologyFile(Issue931Test.class.getResource(OWL_FILE).getPath())
                .propertyFile(Issue931Test.class.getResource(PROPERTIES_FILE).getPath())
                .enableTestMode()
                .build();

        OntopVirtualRepository repo = OntopRepository.defaultRepository(config);
        repo.init();
        CONNECTION = repo.getConnection();
    }

    @AfterClass
    public static void after() {
        if (CONNECTION != null)
            CONNECTION.close();
    }

    /**
     * This query works fine (no OPTIONAL).
     */
    @Test
    public void testGeoJSONWithoutOptional() {
        String query = "PREFIX : <http://example.org/>\n" +
                "PREFIX geo: <http://example.org/geo#>\n" +
                "SELECT ?s ?geojson WHERE {\n" +
                "  ?s geo:asGeoJSON ?geojson .\n" +
                "}\n" +
                "LIMIT 10";

        List<BindingSet> results = runQuery(query);
        assertEquals(2, results.size());
        for (BindingSet bs : results) {
            assertNotNull(bs.getValue("geojson"));
        }
    }

    /**
     * This is the failing query from issue #931: OPTIONAL with xsd:string typed literal
     * combined with multiple OPTIONAL clauses.
     * All 3 subjects have a type and label, but only 2 have geo:asGeoJSON.
     */
    @Test
    public void testGeoJSONWithOptional() {
        String query = "PREFIX : <http://example.org/>\n" +
                "PREFIX geo: <http://example.org/geo#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT ?s ?type ?label ?geojson WHERE {\n" +
                "  ?s a ?type .\n" +
                "  OPTIONAL { ?s rdfs:label ?label }\n" +
                "  OPTIONAL { ?s geo:asGeoJSON ?geojson }\n" +
                "}\n" +
                "LIMIT 10";

        List<BindingSet> results = runQuery(query);
        assertFalse("Query should return results", results.isEmpty());
        assertEquals(3, results.size());
    }

    /**
     * Variant: OPTIONAL with only the xsd:string property (simpler case).
     * All 3 subjects match :Dummy, but only 2 have geo:asGeoJSON.
     */
    @Test
    public void testGeoJSONSingleOptional() {
        String query = "PREFIX : <http://example.org/>\n" +
                "PREFIX geo: <http://example.org/geo#>\n" +
                "SELECT ?s ?geojson WHERE {\n" +
                "  ?s a :Dummy .\n" +
                "  OPTIONAL { ?s geo:asGeoJSON ?geojson }\n" +
                "}\n" +
                "LIMIT 10";

        List<BindingSet> results = runQuery(query);
        assertFalse("Query should return results", results.isEmpty());
        assertEquals(3, results.size());
    }

    private List<BindingSet> runQuery(String queryString) {
        TupleQuery query = CONNECTION.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        TupleQueryResult result = query.evaluate();
        List<BindingSet> results = new ArrayList<>();
        while (result.hasNext()) {
            results.add(result.next());
        }
        result.close();
        return results;
    }
}
