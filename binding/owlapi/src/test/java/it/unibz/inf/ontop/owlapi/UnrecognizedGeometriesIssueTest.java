package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.h2gis.functions.factory.H2GISFunctions;
import org.junit.*;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.*;

public class UnrecognizedGeometriesIssueTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnrecognizedGeometriesIssueTest.class);
    private static final String OBDA_FILE = "src/test/resources/geometries/mapping.obda";
    private static final String PROPERTY_FILE = "src/test/resources/geometries/prop.properties";
    private static final String ONTOLOGY_FILE = "src/test/resources/geometries/ontology.rdf";
    private static final String FACTS_FILE = "src/test/resources/geometries/facts.rdf";
    private static final String DB_FILE = "src/test/resources/geometries/database.sql";

    private Connection sqlConnection;
    private OWLConnection conn;
    private OntopOWLEngine engine;

    @Before
    public void setUp() throws Exception {

        sqlConnection = DriverManager.getConnection("jdbc:h2:mem:geoms", "sa", "");
        H2GISFunctions.load(sqlConnection);
        executeFromFile(sqlConnection, DB_FILE);

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(ONTOLOGY_FILE)
                .nativeOntopMappingFile(OBDA_FILE)
                .propertyFile(PROPERTY_FILE)
                .factsFile(FACTS_FILE)
                .enableTestMode()
                .build();

        engine = new SimpleOntopOWLEngine(config);
        conn = engine.getConnection();

    }

    @After
    public void tearDown() throws Exception {
        conn.close();
        engine.close();
        if (!sqlConnection.isClosed()) {
            try (java.sql.Statement s = sqlConnection.createStatement()) {
                s.execute("DROP ALL OBJECTS DELETE FILES");
            } finally {
                sqlConnection.close();
            }
        }
    }

    @Test
    public void testPointsDistance() throws Exception {
        String query = "PREFIX ex: <http://example.org/>\n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n\n" +
                "SELECT ?v \n" +
                "WHERE {\n" +
                "  ?g1 a ex:Geom; \n" +
                "     geo:asWKT ?wkt1 .\n" +
                "  ?g2 a ex:Geom; \n" +
                "     geo:asWKT ?wkt2 .\n" +
                "  FILTER(?g1 != ?g2) .\n" +
                "  BIND(geof:distance(?wkt1, ?wkt2, uom:metre) AS ?v) .\n" +
                "}";

        assertFalse(runQuery(query).isEmpty());
    }

    private ImmutableSet<String> runQuery(String query) throws Exception {
        ImmutableSet.Builder<String> vValueBuilder = ImmutableSet.builder();

        try (OWLStatement st = conn.createStatement()) {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                OWLBindingSet bindingSet = rs.next();
                Optional.ofNullable(bindingSet.getOWLLiteral("v"))
                        .map(OWLLiteral::getLiteral)
                        .ifPresent(vValueBuilder::add);

                LOGGER.debug(bindingSet + "\n");

            }
            rs.close();
            return vValueBuilder.build();
        }
    }
}
