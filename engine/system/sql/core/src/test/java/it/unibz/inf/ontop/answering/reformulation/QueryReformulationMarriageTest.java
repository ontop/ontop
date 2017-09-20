package it.unibz.inf.ontop.answering.reformulation;


import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.answering.reformulation.input.SelectQuery;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.OntopReformulationSQLConfiguration;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static junit.framework.TestCase.assertFalse;

/**
 * Example showing how to instantiate a QueryReformulator without a QueryEngine
 */
public class QueryReformulationMarriageTest {

    private static final String OBDA_FILE = "src/test/resources/marriage/marriage.obda";
    private static final String CREATE_DB_FILE = "src/test/resources/marriage/create-db.sql";
    private static final String JDBC_URL = "jdbc:h2:mem:questjunitdb";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryReformulationMarriageTest.class);

    /*
     * DB connection (keeps it alive)
     */
    private static Connection CONN;

    @BeforeClass
    public static void setUp() throws Exception {

        CONN = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

        Statement st = CONN.createStatement();

        FileReader reader = new FileReader(CREATE_DB_FILE);
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        CONN.commit();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        CONN.close();
    }

    @Test
    public void testPerson() throws OBDASpecificationException, OntopReformulationException {
        QueryReformulator queryReformulator = createReformulator();
        InputQueryFactory inputQueryFactory = queryReformulator.getInputQueryFactory();

        SelectQuery query = inputQueryFactory.createSelectQuery(
                "PREFIX : <http://example.org/marriage/voc#>\n" +
                "\n" +
                "SELECT DISTINCT ?x \n" +
                "WHERE {\n" +
                "  ?x a :Person .\n" +
                "}");

        SQLExecutableQuery executableQuery = (SQLExecutableQuery) queryReformulator.reformulateIntoNativeQuery(query);
        String sqlQuery = executableQuery.getSQL();

        assertFalse(sqlQuery.isEmpty());
        LOGGER.info(sqlQuery);
    }

    /**
     * Instantiation of the query reformulator
     */
    private static QueryReformulator createReformulator() throws OBDASpecificationException {
        OntopMappingSQLAllConfiguration mappingConfiguration = OntopMappingSQLAllConfiguration.defaultBuilder()
                .nativeOntopMappingFile(OBDA_FILE)
                .jdbcUser(JDBC_USER)
                .jdbcPassword(JDBC_PASSWORD)
                .jdbcUrl(JDBC_URL)
                .enableTestMode()
                .build();

        OBDASpecification obdaSpecification = mappingConfiguration.loadSpecification();

        OntopReformulationSQLConfiguration reformulationConfiguration = OntopReformulationSQLConfiguration.defaultBuilder()
                .obdaSpecification(obdaSpecification)
                .jdbcUser(JDBC_USER)
                .jdbcPassword(JDBC_PASSWORD)
                .jdbcUrl(JDBC_URL)
                .enableTestMode()
                .build();

        return reformulationConfiguration.loadQueryReformulator();
    }
}
