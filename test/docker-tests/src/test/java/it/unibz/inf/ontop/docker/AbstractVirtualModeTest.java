package it.unibz.inf.ontop.docker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.utils.querymanager.QueryController;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerQuery;
import it.unibz.inf.ontop.utils.querymanager.QueryIOManager;
import org.junit.Before;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Common initialization for many tests
 */
public abstract class AbstractVirtualModeTest {

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

    @Before
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
            e.printStackTrace();
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
//            retval = ind1.getLiteral();
            retval = ToStringRenderer.getInstance().getRendering(ind1);

        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        return retval;
    }

    protected boolean runQueryAndReturnBooleanX(String query) throws Exception {
        try (OWLStatement st = conn.createStatement()) {
            BooleanOWLResultSet rs = st.executeAskQuery(query);
            return rs.getValue();
        } finally {
            conn.close();
            reasoner.dispose();
        }
    }

    protected void countResults(String query, int expectedCount) throws OWLException {

        OWLStatement st = conn.createStatement();
        TupleOWLResultSet results = st.executeSelectQuery(query);
        int count = 0;
        while (results.hasNext()) {
            results.next();
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

    protected String checkReturnedValuesAndReturnSql(String query, List<String> expectedValues) throws Exception {

        OntopOWLStatement st = conn.createStatement();
        String sql;

        int i = 0;
        List<String> returnedValues = new ArrayList<>();
        try {
            ExecutableQuery executableQuery = st.getExecutableQuery(query);
            if (! (executableQuery instanceof SQLExecutableQuery))
                throw new IllegalStateException("A SQLExecutableQuery was expected");
            sql = ((SQLExecutableQuery)executableQuery).getSQL();
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                OWLLiteral ind1 = bindingSet.getOWLLiteral("v");
                // log.debug(ind1.toString());
                if (ind1 != null) {
                    returnedValues.add(ind1.getLiteral());
                    System.out.println(ind1.getLiteral());
                    i++;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }

        Collections.sort(returnedValues);
        Collections.sort(expectedValues);

        assertEquals(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),expectedValues, returnedValues);
//        assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
//                returnedValues.equals(expectedValues));
        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size() == i);

        return sql;
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
