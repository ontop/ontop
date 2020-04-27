package it.unibz.inf.ontop.docker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.utils.querymanager.QueryController;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerQuery;
import it.unibz.inf.ontop.utils.querymanager.QueryIOManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Common initialization for many tests
 */
public abstract class AbstractVirtualModeTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected abstract OntopOWLStatement createStatement() throws OWLException;

    protected static OntopOWLReasoner createReasoner(String owlFile, String obdaFile, String propertiesFile) throws OWLOntologyCreationException {
        owlFile = AbstractBindTestWithFunctions.class.getResource(owlFile).toString();
        obdaFile =  AbstractBindTestWithFunctions.class.getResource(obdaFile).toString();
        propertiesFile =  AbstractBindTestWithFunctions.class.getResource(propertiesFile).toString();

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFile)
                .ontologyFile(owlFile)
                .propertyFile(propertiesFile)
                .enableTestMode()
                .build();
        return factory.createReasoner(config);
    }

    protected String runQueryAndReturnStringOfIndividualX(String query) throws OWLException {
        try (OWLStatement st = createStatement(); TupleOWLResultSet rs = st.executeSelectQuery(query)) {
            assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLIndividual ind1 = bindingSet.getOWLIndividual("x");
            return ind1.toString();
        }
    }

    protected String runQueryAndReturnStringOfLiteralX(String query) throws OWLException {
        try (OWLStatement st = createStatement(); TupleOWLResultSet rs = st.executeSelectQuery(query)) {
            assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLLiteral ind1 = bindingSet.getOWLLiteral("x");
            return ToStringRenderer.getInstance().getRendering(ind1);
        }
    }

    protected boolean runQueryAndReturnBooleanX(String query) throws OWLException {
        try (OWLStatement st = createStatement()) {
            BooleanOWLResultSet rs = st.executeAskQuery(query);
            return rs.getValue();
        }
    }

    protected void countResults(int expectedCount, String query) throws OWLException {

        try (OWLStatement st = createStatement(); TupleOWLResultSet results = st.executeSelectQuery(query)) {
            int count = 0;
            while (results.hasNext()) {
                results.next();
                count++;
            }
            assertEquals(expectedCount, count);
        }
    }

    protected boolean checkContainsTuplesSetSemantics(String query,
                                                      ImmutableSet<ImmutableMap<String, String>> expectedTuples)
            throws OWLException {
        try (OWLStatement st = createStatement(); TupleOWLResultSet rs = st.executeSelectQuery(query)) {
            Set<ImmutableMap<String, String>> mutableCopy = new HashSet<>(expectedTuples);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                ImmutableMap<String, String> tuple = getTuple(rs, bindingSet);
                mutableCopy.remove(tuple);
            }
            return mutableCopy.isEmpty();
        }
    }

    protected ImmutableMap<String, String> getTuple(TupleOWLResultSet rs, OWLBindingSet bindingSet) throws OWLException {
        ImmutableMap.Builder<String, String> tuple = ImmutableMap.builder();
        for (String variable : rs.getSignature()) {
            tuple.put(variable, bindingSet.getOWLIndividual(variable).toString());
        }
        return tuple.build();
    }

    protected void checkReturnedUris(List<String> expectedUris, String query) throws Exception {
        try (OWLStatement st = createStatement(); TupleOWLResultSet rs = st.executeSelectQuery(query)) {
            int i = 0;
            List<String> returnedUris = new ArrayList<>();
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                OWLNamedIndividual ind1 = (OWLNamedIndividual) bindingSet.getOWLIndividual("x");

                returnedUris.add(ind1.getIRI().toString());
                log.debug(ind1.getIRI().toString());
                i++;
            }
            assertEquals(String.format("%s instead of \n %s", returnedUris.toString(), expectedUris.toString()), returnedUris, expectedUris);
            assertEquals(String.format("Wrong size: %d (expected %d)", i, expectedUris.size()), expectedUris.size(), i);
        }
    }

    protected void checkThereIsAtLeastOneResult(String query) throws Exception {
        try (OWLStatement st = createStatement(); TupleOWLResultSet rs = st.executeSelectQuery(query)) {
            assertTrue(rs.hasNext());
        }
    }

    protected String checkReturnedValuesAndReturnSql(String query, List<String> expectedValues) throws Exception {
        return checkReturnedValuesAndMayReturnSql(query, true, expectedValues);
    }
    protected void checkReturnedValues(List<String> expectedValues, String query) throws Exception {
        checkReturnedValuesAndMayReturnSql(query, false, expectedValues);
    }

    private String checkReturnedValuesAndMayReturnSql(String query, boolean returnSQL, List<String> expectedValues)
            throws Exception {

        try (OntopOWLStatement st = createStatement()) {
            // Non-final
            String sql = null;
            if (returnSQL) {
                IQ executableQuery = st.getExecutableQuery(query);
                sql = Optional.of(executableQuery.getTree())
                        .filter(t -> t instanceof UnaryIQTree)
                        .map(t -> ((UnaryIQTree) t).getChild().getRootNode())
                        .filter(n -> n instanceof NativeNode)
                        .map(n -> ((NativeNode) n).getNativeQueryString())
                        .orElseThrow(() -> new RuntimeException("Cannot extract the SQL query from\n" + executableQuery));
            }
            int i = 0;
            List<String> returnedValues = new ArrayList<>();
            try (TupleOWLResultSet rs = st.executeSelectQuery(query)) {
                while (rs.hasNext()) {
                    final OWLBindingSet bindingSet = rs.next();
                    OWLLiteral ind1 = bindingSet.getOWLLiteral("v");
                    // log.debug(ind1.toString());
                    if (ind1 != null) {
                        returnedValues.add(ind1.getLiteral());
                        log.debug(ind1.getLiteral());
                        i++;
                    }
                }
            }
            assertEquals(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),expectedValues, returnedValues);
//        assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
//                returnedValues.equals(expectedValues));
            assertEquals(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size(), i);

            // May be null
            return sql;
        }
    }

    protected void runQueries(String queryFileName) throws Exception {

        try (OWLStatement st = createStatement()) {
            QueryController qc = new QueryController();
            QueryIOManager qman = new QueryIOManager(qc);
            qman.load(queryFileName);

            for (QueryControllerGroup group : qc.getGroups()) {
                for (QueryControllerQuery query : group.getQueries()) {

                    log.debug("Executing query: {}", query.getID());
                    log.debug("Query: \n{}", query.getQuery());

                    long start = System.nanoTime();
                    try (TupleOWLResultSet res = st.executeSelectQuery(query.getQuery())) {
                        long end = System.nanoTime();
                        long time = (end - start) / 1000;

                        int count = 0;
                        while (res.hasNext()) {
                            count += 1;
                        }
                        log.debug("Total result: {}", count);
                        assertNotEquals(0, count);
                        log.debug("Elapsed time: {} ms", time);
                    }
                }
            }
        }
    }

    protected void runQuery(String query) throws Exception {
        try (OntopOWLStatement st = createStatement()) {
            long t1 = System.nanoTime();
            try (TupleOWLResultSet rs = st.executeSelectQuery(query)) {
                while (rs.hasNext()) {
                    final OWLBindingSet bindingSet = rs.next();
                    log.debug(bindingSet.toString());
                }
            }
            long t2 = System.nanoTime();

            /*
             * Print the query summary
             */
            IQ iq = st.getExecutableQuery(query);
            String sqlQuery = Optional.of(iq.getTree())
                    .filter(t -> t instanceof UnaryIQTree)
                    .map(t -> ((UnaryIQTree) t).getChild().getRootNode())
                    .filter(n -> n instanceof NativeNode)
                    .map(n -> ((NativeNode) n).getNativeQueryString())
                    .orElseThrow(() -> new RuntimeException("Cannot extract the SQL query: " + iq));
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
            log.info((t2 - t1)/1000 + "ms");
        }
    }
}
