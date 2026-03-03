package it.unibz.inf.ontop.materialization;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLConfiguration;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import org.junit.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OnePassMaterializerTest {

    private static final String jdbcUrl = "jdbc:h2:mem:" + UUID.randomUUID();
    private static final String dbFile = "/one-pass-materializer/db.sql";
    private static final String basicMappingFile = "/one-pass-materializer/mapping-basic.obda";
    private static final String emptyMappingFile = "/one-pass-materializer/mapping-empty.obda";
    private static final String subclassMappingFile = "/one-pass-materializer/mapping-subclasses.obda";
    private static final String filterMappingFile = "/one-pass-materializer/mapping-filter.obda";
    private static final String joinMappingFile = "/one-pass-materializer/mapping-join.obda";
    private static final String graphMappingFile = "/one-pass-materializer/mapping-graphs.obda";

    private static Connection conn;
    private static OntopStandaloneSQLConfiguration.Builder<?> defaultConfigurationBuilder;

    public OnePassMaterializerTest() {
         defaultConfigurationBuilder = OntopStandaloneSQLConfiguration.defaultBuilder()
                 .jdbcUrl(jdbcUrl)
                 .jdbcUser("sa")
                 .jdbcPassword("")
                 .enableTestMode();
    }

    @BeforeClass
    public static void createDB() throws SQLException, IOException {
        conn = DriverManager.getConnection(jdbcUrl, "sa", "");

        java.sql.Statement st = conn.createStatement();

        FileReader reader = new FileReader(OnePassMaterializerTest.class.getResource(dbFile).getPath());
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        conn.commit();
    }

    @AfterClass
    public static void closeConnection() throws SQLException {
        conn.close();
    }

    @Test
    public void testBasicMapping() throws Exception {
        OntopStandaloneSQLConfiguration defaultConfiguration = defaultConfigurationBuilder
                .nativeOntopMappingFile(OnePassMaterializerTest.class.getResource(basicMappingFile).getPath())
                .build();

        MaterializationParams onePassParams = MaterializationParams.defaultBuilder()
                .build();
        MaterializationResult onePassResults = runMaterializer(defaultConfiguration, onePassParams);

        MaterializationParams legacyParams = MaterializationParams.defaultBuilder()
                .useLegacyMaterializer(true)
                .build();
        MaterializationResult legacyResults = runMaterializer(defaultConfiguration, legacyParams);

        assertEquals(3, onePassResults.queryCount);
        assertTrue(onePassResults.tripleCount == legacyResults.tripleCount && legacyResults.tripleCount == 46);
    }

    @Test
    public void testEmptyResultSet() throws Exception {
        OntopStandaloneSQLConfiguration defaultConfiguration = defaultConfigurationBuilder
                .nativeOntopMappingFile(OnePassMaterializerTest.class.getResource(emptyMappingFile).getPath())
                .build();

        MaterializationParams onePassParams = MaterializationParams.defaultBuilder()
                .build();
        MaterializationResult onePassResults = runMaterializer(defaultConfiguration, onePassParams);

        MaterializationParams legacyParams = MaterializationParams.defaultBuilder()
                .useLegacyMaterializer(true)
                .build();
        MaterializationResult legacyResults = runMaterializer(defaultConfiguration, legacyParams);

        assertEquals(2, onePassResults.queryCount);
        assertTrue(onePassResults.tripleCount == legacyResults.tripleCount && legacyResults.tripleCount == 0);
    }

    @Test
    public void testSubclassesMapping() throws Exception {
        OntopStandaloneSQLConfiguration defaultConfiguration = defaultConfigurationBuilder
                .nativeOntopMappingFile(OnePassMaterializerTest.class.getResource(subclassMappingFile).getPath())
                .build();

        MaterializationParams onePassParams = MaterializationParams.defaultBuilder()
                .build();
        MaterializationResult onePassResults = runMaterializer(defaultConfiguration, onePassParams);

        MaterializationParams legacyParams = MaterializationParams.defaultBuilder()
                .useLegacyMaterializer(true)
                .build();
        MaterializationResult legacyResults = runMaterializer(defaultConfiguration, legacyParams);

        assertEquals(1, onePassResults.queryCount);
        assertTrue(onePassResults.tripleCount == legacyResults.tripleCount && legacyResults.tripleCount == 8);
    }

    @Test
    public void testFilterMapping() throws Exception {
        OntopStandaloneSQLConfiguration defaultConfiguration = defaultConfigurationBuilder
                .nativeOntopMappingFile(OnePassMaterializerTest.class.getResource(filterMappingFile).getPath())
                .build();

        MaterializationParams onePassParams = MaterializationParams.defaultBuilder()
                .build();
        MaterializationResult onePassResults = runMaterializer(defaultConfiguration, onePassParams);

        MaterializationParams legacyParams = MaterializationParams.defaultBuilder()
                .useLegacyMaterializer(true)
                .build();
        MaterializationResult legacyResults = runMaterializer(defaultConfiguration, legacyParams);

        assertEquals(3, onePassResults.queryCount);
        assertTrue(onePassResults.tripleCount == legacyResults.tripleCount && legacyResults.tripleCount == 16);
    }

    // number of queries could be further reduced by recognizing self joins as eligible for being merged
    @Test
    public void testJoinMappingWithDuplicates() throws Exception {
        OntopStandaloneSQLConfiguration defaultConfiguration = defaultConfigurationBuilder
                .nativeOntopMappingFile(OnePassMaterializerTest.class.getResource(joinMappingFile).getPath())
                .build();

        MaterializationParams onePassParams = MaterializationParams.defaultBuilder()
                .allowDuplicates(true)
                .build();
        MaterializationResult onePassResults = runMaterializer(defaultConfiguration, onePassParams);

        MaterializationParams legacyParams = MaterializationParams.defaultBuilder()
                .useLegacyMaterializer(true)
                .build();
        MaterializationResult legacyResults = runMaterializer(defaultConfiguration, legacyParams);

        assertEquals(5, onePassResults.queryCount);
        assertEquals(31, onePassResults.tripleCount);
        assertEquals(30, legacyResults.tripleCount);
    }

    @Test
    public void testJoinMappingNoDuplicates() throws Exception {
        OntopStandaloneSQLConfiguration defaultConfiguration = defaultConfigurationBuilder
                .nativeOntopMappingFile(OnePassMaterializerTest.class.getResource(joinMappingFile).getPath())
                .build();

        MaterializationParams onePassParams = MaterializationParams.defaultBuilder()
                .build();
        MaterializationResult onePassResults = runMaterializer(defaultConfiguration, onePassParams);

        MaterializationParams legacyParams = MaterializationParams.defaultBuilder()
                .useLegacyMaterializer(true)
                .build();
        MaterializationResult legacyResults = runMaterializer(defaultConfiguration, legacyParams);

        assertEquals(8, onePassResults.queryCount);
        assertTrue(onePassResults.tripleCount == legacyResults.tripleCount && legacyResults.tripleCount == 30);
    }

    // number of queries could be further reduced by recognizing joins where one of the children is a VALUES node as eligible for being merged
    @Test
    public void testGraphMapping() throws Exception {
        OntopStandaloneSQLConfiguration defaultConfiguration = defaultConfigurationBuilder
                .nativeOntopMappingFile(OnePassMaterializerTest.class.getResource(graphMappingFile).getPath())
                .build();

        MaterializationParams onePassParams = MaterializationParams.defaultBuilder()
                .build();
        MaterializationResult onePassResults = runMaterializer(defaultConfiguration, onePassParams);

        MaterializationParams legacyParams = MaterializationParams.defaultBuilder()
                .useLegacyMaterializer(true)
                .build();
        MaterializationResult legacyResults = runMaterializer(defaultConfiguration, legacyParams);

        assertEquals(3, onePassResults.queryCount);
        assertTrue(onePassResults.tripleCount == legacyResults.tripleCount && legacyResults.tripleCount == 30);
    }


    private MaterializationResult runMaterializer(OntopStandaloneSQLConfiguration configuration, MaterializationParams params)
            throws OBDASpecificationException, OntopConnectionException, OntopQueryAnsweringException {
        OntopRDFMaterializer materializer = OntopRDFMaterializer.defaultMaterializer(configuration, params);

        ImmutableList.Builder<RDFFact> rdfGraphBuilder = ImmutableList.builder();
        long queryCount;
        long tripleCount;
        try (MaterializedGraphResultSet materializationResultSet = materializer.materialize()) {
            while (materializationResultSet.hasNext()) {
                RDFFact fact = materializationResultSet.next();
                rdfGraphBuilder.add(fact);
            }
            queryCount = materializationResultSet.getSQLQueryCountSoFar();
            tripleCount = materializationResultSet.getTripleCountSoFar();
        }

        return new MaterializationResult(queryCount, tripleCount, rdfGraphBuilder.build());
    }

    private static class MaterializationResult {
        private final long queryCount;
        private final long tripleCount;
        private final ImmutableList<RDFFact> rdfGraph;

        public MaterializationResult(long queryCount, long tripleCount, ImmutableList<RDFFact> rdfGraph) {
            this.queryCount = queryCount;
            this.tripleCount = tripleCount;
            this.rdfGraph = rdfGraph;
        }
    }
}
