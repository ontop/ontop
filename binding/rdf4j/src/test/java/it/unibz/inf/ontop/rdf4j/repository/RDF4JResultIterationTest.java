package it.unibz.inf.ontop.rdf4j.repository;

import static org.junit.Assert.assertTrue;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

/**
 * Tests that user-applied constraints can be provided through
 * sesameWrapper.OntopVirtualRepository
 * with manually instantiated metadata.
 * <p>
 * This is quite similar to the setting in the optique platform
 * <p>
 * Some stuff copied from ExampleManualMetadata
 *
 * @author dhovl
 */
  /*GUOHUI: 2016-01-01: For some strange unknown reason, this test often fails on Travis-CI:
   <pre>
     Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.11 sec <<< FAILURE! - in it.unibz.inf.ontop.sql.SesameResultIterationTest
     testIteration(it.unibz.inf.ontop.sql.SesameResultIterationTest)  Time elapsed: 0.108 sec  <<< FAILURE!
     java.lang.AssertionError: null
     at org.junit.Assert.fail(Assert.java:86)        //   assertTrue(longTime > shortTime);
     at org.junit.Assert.assertTrue(Assert.java:41)
     at org.junit.Assert.assertTrue(Assert.java:52)
     at it.unibz.inf.ontop.sql.SesameResultIterationTest.testIteration(SesameResultIterationTest.java:156)
     </pre>

    Therefore, I disable this test until we can solve it
    */
@Ignore
public class RDF4JResultIterationTest {
    static String owlfile = "src/test/resources/userconstraints/uc.owl";
    static String r2rmlfile = "src/test/resources/userconstraints/uc.ttl";

    static String uc_keyfile = "src/test/resources/userconstraints/keys.lst";
    static String uc_create = "src/test/resources/userconstraints/create.sql";

    private static final String URL = "jdbc:h2:mem:countries_iteration_test";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private Connection sqlConnection;
    private RepositoryConnection conn;


    @Before
    public void init() throws Exception {
        sqlConnection = DriverManager.getConnection(URL, USER, PASSWORD);

        try (java.sql.Statement s = sqlConnection.createStatement()) {
            try (Scanner sqlFile = new Scanner(new File(uc_create))) {
                String text = sqlFile.useDelimiter("\\A").next();
                s.execute(text);
            }

            for (int i = 1; i <= 10; i++) {
                s.execute("INSERT INTO TABLE1 VALUES (" + i + "," + i + ");");
            }
        }


        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlfile)
                .r2rmlMappingFile(r2rmlfile)
                .jdbcUrl(URL)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode()
                .build();

        OntopRepository repo = OntopRepository.defaultRepository(configuration);
        repo.initialize();
        /*
		 * Prepare the data connection for querying.
		 */
        conn = repo.getConnection();


    }


    @After
    public void tearDown() throws Exception {
        if (!sqlConnection.isClosed()) {
            try (java.sql.Statement s = sqlConnection.createStatement()) {
                s.execute("DROP ALL OBJECTS DELETE FILES");
            }
            finally {
                sqlConnection.close();
            }
        }
    }


    /**
     * This tests that not extracting any rows from the result set is actually faster (in milliseconds resolution)
     * than not extracting any.
     * <p>
     * Starts with querying, to warm up the system
     *
     * @throws Exception
     */
    @Test
    public void testIteration() throws Exception {
        String queryString = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1. ?y :hasVal1 ?v2.}";

        // execute query
        Query query = conn.prepareQuery(QueryLanguage.SPARQL, queryString);

        TupleQuery tq = (TupleQuery) query;
        TupleQueryResult result = tq.evaluate();
        assertTrue(result.hasNext());
        result.next();
        result.close();

        long start = System.currentTimeMillis();
        result = tq.evaluate();
        result.close();
        long shortTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        result = tq.evaluate();
        while (result.hasNext())
            result.next();
        result.close();
        long longTime = System.currentTimeMillis() - start;
        System.out.println("Long " + longTime + "Short: " + shortTime);

        assertTrue(longTime > shortTime);

    }

}
