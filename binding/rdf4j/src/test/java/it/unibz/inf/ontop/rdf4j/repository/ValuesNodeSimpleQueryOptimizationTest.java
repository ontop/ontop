package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.answering.reformulation.input.SelectQuery;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopReformulationSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;

/**
 * Run tests on whether the SQL translation of the Slice Optimization for Values Node
 * works properly. Simple ontology with one table and one mapping used.
 */
public class ValuesNodeSimpleQueryOptimizationTest {

    // First example with one mapping and table
    private static final String MAPPING_FILE = "/values-node/mapping.obda";
    private static final String ONTOLOGY_FILE = "/values-node/ontology.owl";
    private static final String SQL_SCRIPT = "/values-node/example.sql";

    private static final String URL_PREFIX = "jdbc:h2:mem:";
    private static final String JDBC_URL = URL_PREFIX + UUID.randomUUID().toString();
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static OntopRepositoryConnection REPO_CONNECTION;
    private static Connection SQL_CONNECTION;



    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA();
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testTranslatedSQLQuery1() throws OBDASpecificationException, OntopReformulationException, SQLException, IOException {
        String SPARQL_Query_String = "SELECT ?v WHERE {\n" +
                "  ?v ?p ?o .\n" +
                "}" +
                "LIMIT 2";

        String expectedSQLQueryTranslation = "SELECT V1.C1 AS \"v1m6\"\n" +
                        "FROM (VALUES  ('http://te.st/ValuesNodeTest#student/Anna'), ('http://te.st/ValuesNodeTest#student/Francis') AS V1 )\n";

        assertEquals(expectedSQLQueryTranslation, generateSQLTranslation(SPARQL_Query_String));
    }

    private String generateSQLTranslation(String SPARQL_query) throws OBDASpecificationException, OntopReformulationException, SQLException, IOException {
        QueryReformulator queryReformulator = createReformulator();
        InputQueryFactory inputQueryFactory = queryReformulator.getInputQueryFactory();

        SelectQuery query = inputQueryFactory.createSelectQuery(SPARQL_query);

        IQ executableQuery = queryReformulator.reformulateIntoNativeQuery(query,
                queryReformulator.getQueryLoggerFactory().create(ImmutableMultimap.of()));

        return Optional.of(executableQuery.getTree())
                .filter(t -> t instanceof UnaryIQTree)
                .map(t -> ((UnaryIQTree) t).getChild().getRootNode())
                .filter(n -> n instanceof NativeNode)
                .map(n -> ((NativeNode) n).getNativeQueryString())
                .orElseThrow(() -> new RuntimeException("Cannot extract the SQL query from\n" + executableQuery));
    }

    private QueryReformulator createReformulator() throws OBDASpecificationException, SQLException, IOException {
        OBDASpecification obdaSpecification = loadOBDASpecification();

        OntopReformulationSQLConfiguration reformulationConfiguration = OntopReformulationSQLConfiguration.defaultBuilder()
                .obdaSpecification(obdaSpecification)
                .jdbcUrl(JDBC_URL)
                .enableTestMode()
                .build();

        return reformulationConfiguration.loadQueryReformulator();
    }

    private OBDASpecification loadOBDASpecification() throws OBDASpecificationException, SQLException, IOException {
        OntopSQLOWLAPIConfiguration mappingConfiguration = instantiateBuilder().build();

        return mappingConfiguration.loadSpecification();
    }

    protected static void initOBDA() throws SQLException, IOException {
        SQL_CONNECTION = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);

        java.sql.Statement st = SQL_CONNECTION.createStatement();

        FileReader reader = new FileReader(ValuesNodeSimpleQueryOptimizationTest.class.getResource(SQL_SCRIPT).getPath());
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        SQL_CONNECTION.commit();

        OntopSQLOWLAPIConfiguration.Builder<? extends OntopSQLOWLAPIConfiguration.Builder> builder =
                instantiateBuilder();

        OntopSQLOWLAPIConfiguration config = builder.build();

        OntopVirtualRepository repo = OntopRepository.defaultRepository(config);
        repo.init();

        REPO_CONNECTION = repo.getConnection();
    }

    private static OntopSQLOWLAPIConfiguration.Builder<? extends OntopSQLOWLAPIConfiguration.Builder> instantiateBuilder() {
        OntopSQLOWLAPIConfiguration.Builder<? extends OntopSQLOWLAPIConfiguration.Builder> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(ValuesNodeSimpleQueryOptimizationTest.class.getResource(ONTOLOGY_FILE).getPath())
                .nativeOntopMappingFile(ValuesNodeSimpleQueryOptimizationTest.class.getResource(MAPPING_FILE).getPath())
                .jdbcUrl(JDBC_URL)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode();
        return builder;
    }

    protected static void release() throws SQLException {
        REPO_CONNECTION.close();
        SQL_CONNECTION.close();
    }
}
