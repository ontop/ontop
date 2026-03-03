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

public class OnePassMaterializerTest {

    private static final String jdbcUrl = "jdbc:h2:mem:" + UUID.randomUUID();
    private static final String dbFile = "/one-pass-materializer/db.sql";
    private static final String basicMappingFile = "/one-pass-materializer/mapping-basic.obda";
    private static final String emptyMappingFile = "/one-pass-materializer/mapping-empty.obda";
    private static final String subclassMappingFile = "/one-pass-materializer/mapping-subclasses.obda";
    private static final String filterMappingFile = "/one-pass-materializer/mapping-filter.obda";
    private static final String joinMappingFile = "/one-pass-materializer/mapping-join.obda";

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
        MaterializationParams params = MaterializationParams.defaultBuilder()
                .build();

        runMaterializerAndCount(defaultConfiguration, params, 3, 46);
    }

    @Test
    public void testEmptyResultSet() throws Exception {
        OntopStandaloneSQLConfiguration defaultConfiguration = defaultConfigurationBuilder
                .nativeOntopMappingFile(OnePassMaterializerTest.class.getResource(emptyMappingFile).getPath())
                .build();
        MaterializationParams params = MaterializationParams.defaultBuilder()
                .build();

        runMaterializerAndCount(defaultConfiguration, params, 2, 0);
    }

    @Test
    public void testSubclassesMapping() throws Exception {
        OntopStandaloneSQLConfiguration defaultConfiguration = defaultConfigurationBuilder
                .nativeOntopMappingFile(OnePassMaterializerTest.class.getResource(subclassMappingFile).getPath())
                .build();
        MaterializationParams params = MaterializationParams.defaultBuilder()
                .build();

        runMaterializerAndCount(defaultConfiguration, params, 1, 8);
    }

    @Test
    public void testFilterMapping() throws Exception {
        OntopStandaloneSQLConfiguration defaultConfiguration = defaultConfigurationBuilder
                .nativeOntopMappingFile(OnePassMaterializerTest.class.getResource(filterMappingFile).getPath())
                .build();
        MaterializationParams params = MaterializationParams.defaultBuilder()
                .build();

        runMaterializerAndCount(defaultConfiguration, params, 3, 16);
    }

    @Test
    public void testJoinMapping() throws Exception {
        OntopStandaloneSQLConfiguration defaultConfiguration = defaultConfigurationBuilder
                .nativeOntopMappingFile(OnePassMaterializerTest.class.getResource(joinMappingFile).getPath())
                .build();
        MaterializationParams params = MaterializationParams.defaultBuilder()
                .allowDuplicates(true)
                .build();

        runMaterializerAndCount(defaultConfiguration, params, 5, 31);
    }


    private static void runMaterializerAndCount(OntopStandaloneSQLConfiguration configuration, MaterializationParams params,
                                                long queryCount, long tripleCount) throws OBDASpecificationException, OntopConnectionException, OntopQueryAnsweringException {
        OntopRDFMaterializer materializer = OntopRDFMaterializer.defaultMaterializer(configuration, params);

        try (MaterializedGraphResultSet materializationResultSet = materializer.materialize()) {

            ImmutableList.Builder<RDFFact> rdfGraphBuilder = ImmutableList.builder();
            while (materializationResultSet.hasNext()) {
                RDFFact fact = materializationResultSet.next();
                rdfGraphBuilder.add(fact);
                System.out.println(fact);
            }

            assertEquals(queryCount, materializationResultSet.getSQLQueryCountSoFar());
            assertEquals(tripleCount, materializationResultSet.getTripleCountSoFar());
        }
    }
}
