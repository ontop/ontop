package it.unibz.inf.ontop.docker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.utils.querymanager.QueryIOManager;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.utils.querymanager.QueryController;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerQuery;
import junit.framework.TestCase;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Common initialization for many tests
 */
public abstract class AbstractVirtualModeTest extends TestCase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String owlFileName;
    private final String obdaFileName;
    private final String propertyFileName;

    protected OntopOWLReasoner reasoner;
    protected OntopOWLConnection conn;

    public AbstractVirtualModeTest(String owlFile, String obdaFile, String propertyFile) {
        this.owlFileName = this.getClass().getResource(owlFile).toString();
        this.obdaFileName = this.getClass().getResource(obdaFile).toString();
        this.propertyFileName = this.getClass().getResource(propertyFile).toString();
    }

    @Override
    public void setUp() throws Exception {
        // Creating a new instance of the reasoner
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
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
        OWLStatement st = conn.createStatement();
        String retval;
        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);

            assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLIndividual ind1 = bindingSet.getOWLIndividual("x");
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
        OWLStatement st = conn.createStatement();
        String retval;
        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);

            assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLLiteral ind1 = bindingSet.getOWLLiteral("x");
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
        OWLStatement st = conn.createStatement();
        boolean retval;
        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLLiteral ind1 = bindingSet.getOWLLiteral("x");
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

        OWLStatement st = conn.createStatement();
        TupleOWLResultSet results = st.executeSelectQuery(query);
        int count = 0;
        while (results.hasNext()) {
            count++;
        }
        assertEquals(expectedCount, count);
    }

    protected boolean checkContainsTuplesSetSemantics(String query, ImmutableSet<ImmutableMap<String, String>> expectedTuples)
            throws Exception {
        HashSet<ImmutableMap<String, String>> mutableCopy = new HashSet<>(expectedTuples);
        OWLStatement st = conn.createStatement();
        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                ImmutableMap<String, String> tuple = getTuple(rs, bindingSet);
                if (mutableCopy.contains(tuple)) {
                    mutableCopy.remove(tuple);
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        return mutableCopy.isEmpty();
    }

    protected ImmutableMap<String, String> getTuple(TupleOWLResultSet rs, OWLBindingSet bindingSet) throws OWLException {
        ImmutableMap.Builder<String, String> tuple = ImmutableMap.builder();
        for (String variable : rs.getSignature()) {
            tuple.put(variable, bindingSet.getOWLIndividual(variable).toString());
        }
        return tuple.build();
    }

    protected void checkReturnedUris(String query, List<String> expectedUris) throws Exception {
        OWLStatement st = conn.createStatement();
        int i = 0;
        List<String> returnedUris = new ArrayList<>();
        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                OWLNamedIndividual ind1 = (OWLNamedIndividual) bindingSet.getOWLIndividual("x");

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
        OWLStatement st = conn.createStatement();
        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            assertTrue(rs.hasNext());

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
        OWLStatement st = conn.createStatement();
        boolean retval;
        try {
            // FIXME: use propery ask query
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLLiteral ind1 = bindingSet.getOWLLiteral(1);
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

        OWLStatement st = conn.createStatement();

        QueryController qc = new QueryController();
        QueryIOManager qman = new QueryIOManager(qc);
        qman.load(queryFileName);

        for (QueryControllerGroup group : qc.getGroups()) {
            for (QueryControllerQuery query : group.getQueries()) {

                log.debug("Executing query: {}", query.getID());
                log.debug("Query: \n{}", query.getQuery());

                long start = System.nanoTime();
                TupleOWLResultSet res = st.executeSelectQuery(query.getQuery());
                long end = System.nanoTime();

                double time = (end - start) / 1000;

                int count = 0;
                while (res.hasNext()) {
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
        TupleOWLResultSet rs = st.executeSelectQuery(query);

        int columnSize = rs.getColumnCount();
        while (rs.hasNext()) {
            final OWLBindingSet bindingSet = rs.next();
            for (int idx = 1; idx <= columnSize; idx++) {
                OWLObject binding = bindingSet.getOWLObject(idx);
                log.debug(binding.toString() + ", ");
            }

        }
        rs.close();
        long t2 = System.currentTimeMillis();

                /* 
                * Print the query summary 
                */
        String sqlQuery = ((SQLExecutableQuery) st.getExecutableQuery(query)).getSQL();
        log.info("");
        log.info("The input SPARQL query:");
        log.info("=======================");
        log.info(query);
        log.info("");
        log.info("The output SQL query:");
        log.info("=====================");
        log.info(sqlQuery);
        log.info("Query Execution Time:");
        log.info("=====================");
        log.info((t2 - t1) + "ms");
    }
}
