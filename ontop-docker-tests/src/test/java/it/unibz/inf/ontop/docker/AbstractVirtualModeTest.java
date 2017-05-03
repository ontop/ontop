package it.unibz.inf.ontop.docker;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.io.QueryIOManager;
import it.unibz.inf.ontop.owlrefplatform.core.SQLExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.querymanager.QueryController;
import it.unibz.inf.ontop.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.querymanager.QueryControllerQuery;
import junit.framework.TestCase;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Common initialization for many tests
 */
public abstract class AbstractVirtualModeTest extends TestCase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String owlFileName;
    private final String obdaFileName;
    private final String propertyFileName;

    protected QuestOWL reasoner;
    protected OntopOWLConnection conn;

    public AbstractVirtualModeTest(String owlFile, String obdaFile, String propertyFile) {
        this.owlFileName =  this.getClass().getResource(owlFile).toString();
        this.obdaFileName =  this.getClass().getResource(obdaFile).toString();
        this.propertyFileName =  this.getClass().getResource(propertyFile).toString();
    }

    @Override
    public void setUp() throws Exception {
        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .enableFullMetadataExtraction(false)
                .ontologyFile(owlFileName)
                .nativeOntopMappingFile(obdaFileName)
                .propertyFile(propertyFileName)
                .enableTestMode()
                .build();
        reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        conn = reasoner.getConnection();
    }

    public void tearDown() throws Exception {
        conn.close();
        reasoner.dispose();
    }

    protected String runQueryAndReturnStringOfIndividualX(String query) throws Exception {
        OntopOWLStatement st = conn.createStatement();
        String retval;
        try {
            QuestOWLResultSet rs = st.executeTuple(query);

            assertTrue(rs.nextRow());
            OWLIndividual ind1 = rs.getOWLIndividual("x");
            retval = ind1.toString();

        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        return retval;
    }

    protected String runQueryAndReturnStringOfLiteralX(String query) throws Exception {
        OntopOWLStatement st = conn.createStatement();
        String retval;
        try {
            QuestOWLResultSet rs = st.executeTuple(query);

            assertTrue(rs.nextRow());
            OWLLiteral ind1 = rs.getOWLLiteral("x");
            retval = ind1.toString();

        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        return retval;
    }

    protected boolean runQueryAndReturnBooleanX(String query) throws Exception {
        OntopOWLStatement st = conn.createStatement();
        boolean retval;
        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            assertTrue(rs.nextRow());
            OWLLiteral ind1 = rs.getOWLLiteral("x");
            retval = ind1.parseBoolean();
        } catch (Exception e) {
            throw e;
        } finally {
            try {

            } catch (Exception e) {
                st.close();
                assertTrue(false);
            }
            conn.close();
            reasoner.dispose();
        }
        return retval;
    }

    protected void countResults(String query, int expectedCount) throws OWLException {

        OntopOWLStatement st = conn.createStatement();
        QuestOWLResultSet results = st.executeTuple(query);
        int count = 0;
        while (results.nextRow()) {
            count++;
        }
        assertEquals(expectedCount, count);
    }

    protected void checkReturnedUris(String query, List<String> expectedUris) throws Exception {
        OntopOWLStatement st = conn.createStatement();
        int i = 0;
        List<String> returnedUris = new ArrayList<>();
        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            while (rs.nextRow()) {
                OWLNamedIndividual ind1 = (OWLNamedIndividual) rs.getOWLIndividual("x");

                returnedUris.add(ind1.getIRI().toString());
                log.debug(ind1.getIRI().toString());
                i++;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        assertTrue(String.format("%s instead of \n %s", returnedUris.toString(), expectedUris.toString()),
                returnedUris.equals(expectedUris));
        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedUris.size()), expectedUris.size() == i);
    }

    protected void checkThereIsAtLeastOneResult(String query) throws Exception {
        OntopOWLStatement st = conn.createStatement();
        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            assertTrue(rs.nextRow());

        } catch (Exception e) {
            throw e;
        } finally {
            try {

            } catch (Exception e) {
                st.close();
                assertTrue(false);
            }
            conn.close();
            reasoner.dispose();
        }
    }

    protected boolean runASKTests(String query) throws Exception {
        OntopOWLStatement st = conn.createStatement();
        boolean retval;
        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            assertTrue(rs.nextRow());
            OWLLiteral ind1 = rs.getOWLLiteral(1);
            retval = ind1.parseBoolean();
        } catch (Exception e) {
            throw e;
        } finally {
            try {

            } catch (Exception e) {
                st.close();
                assertTrue(false);
            }
            conn.close();
            reasoner.dispose();
        }
        return retval;
    }

    protected void runQueries(String queryFileName) throws Exception {

        OntopOWLStatement st = conn.createStatement();

        QueryController qc = new QueryController();
        QueryIOManager qman = new QueryIOManager(qc);
        qman.load(queryFileName);

        for (QueryControllerGroup group : qc.getGroups()) {
            for (QueryControllerQuery query : group.getQueries()) {

                log.debug("Executing query: {}", query.getID());
                log.debug("Query: \n{}", query.getQuery());

                long start = System.nanoTime();
                QuestOWLResultSet res = st.executeTuple(query.getQuery());
                long end = System.nanoTime();

                double time = (end - start) / 1000;

                int count = 0;
                while (res.nextRow()) {
                    count += 1;
                }
                log.debug("Total result: {}", count);
                assertFalse(count == 0);
                log.debug("Elapsed time: {} ms", time);
            }
        }
    }

    protected void runQuery(String query) throws Exception {
        long t1 = System.currentTimeMillis();

        OntopOWLStatement st = conn.createStatement();
        QuestOWLResultSet rs = st.executeTuple(query);

        int columnSize = rs.getColumnCount();
        while (rs.nextRow()) {
            for (int idx = 1; idx <= columnSize; idx++) {
                OWLObject binding = rs.getOWLObject(idx);
                log.debug(binding.toString() + ", ");
            }

        }
        rs.close();
        long t2 = System.currentTimeMillis();

                /* 
                * Print the query summary 
                */
        String sqlQuery = ((SQLExecutableQuery) st.getExecutableQuery(query)).getSQL();
        System.out.println();
        System.out.println("The input SPARQL query:");
        System.out.println("=======================");
        System.out.println(query);
        System.out.println();
        System.out.println("The output SQL query:");
        System.out.println("=====================");
        System.out.println(sqlQuery);
        System.out.println("Query Execution Time:");
        System.out.println("=====================");
        System.out.println((t2-t1) + "ms");
    }
}
