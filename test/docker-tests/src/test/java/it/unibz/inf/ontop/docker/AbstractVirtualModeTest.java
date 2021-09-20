package it.unibz.inf.ontop.docker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
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
        return createReasoner(owlFile,obdaFile, propertiesFile,Optional.empty());
    }

    protected static OntopOWLReasoner createReasonerWithConstraints(String owlFile, String obdaFile, String propertiesFile, String implicitConstraintsFile) throws OWLOntologyCreationException {
        return createReasoner(owlFile,obdaFile,propertiesFile,Optional.of(implicitConstraintsFile));
    }

    private static OntopOWLReasoner createReasoner(String owlFile, String obdaFile, String propertiesFile, Optional<String> optionalImplicitConstraintsFile) throws OWLOntologyCreationException {
        owlFile = AbstractVirtualModeTest.class.getResource(owlFile).toString();
        obdaFile =  AbstractVirtualModeTest.class.getResource(obdaFile).toString();
        propertiesFile =  AbstractVirtualModeTest.class.getResource(propertiesFile).toString();

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = createConfig(owlFile, obdaFile, propertiesFile, optionalImplicitConstraintsFile);
        return factory.createReasoner(config);
    }

    private static OntopSQLOWLAPIConfiguration createConfig(String owlFile, String obdaFile, String propertiesFile, Optional<String> optionalImplicitConstraintsFile) {

        if(optionalImplicitConstraintsFile.isPresent()) {
            return OntopSQLOWLAPIConfiguration.defaultBuilder()
                    .nativeOntopMappingFile(obdaFile)
                    .ontologyFile(owlFile)
                    .propertyFile(propertiesFile)
                    .basicImplicitConstraintFile(
                            AbstractVirtualModeTest.class.getResource(optionalImplicitConstraintsFile.get()).toString())
                    .enableTestMode()
                    .build();
        }

        return OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFile)
                .ontologyFile(owlFile)
                .propertyFile(propertiesFile)
                .enableTestMode()
                .build();
    }

    protected static OntopOWLReasoner createR2RMLReasoner(String owlFile, String r2rmlFile, String propertiesFile) throws OWLOntologyCreationException {
        owlFile = AbstractVirtualModeTest.class.getResource(owlFile).toString();
        r2rmlFile =  AbstractVirtualModeTest.class.getResource(r2rmlFile).toString();
        propertiesFile = AbstractVirtualModeTest.class.getResource(propertiesFile).toString();

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .r2rmlMappingFile(r2rmlFile)
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

    protected void checkContainsAllSetSemanticsWithErrorMessage(String query,
                                                                ImmutableSet<ImmutableMap<String, String>> expectedTuples)
            throws OWLException {
            try (OWLStatement st = createStatement(); TupleOWLResultSet rs = st.executeSelectQuery(query)) {
                Set<ImmutableMap<String, String>> mutableCopy = new HashSet<>(expectedTuples);
                LinkedList<ImmutableMap<String, String>> returnedAnswers = new LinkedList<>();
                while (rs.hasNext()) {
                    final OWLBindingSet bindingSet = rs.next();
                    ImmutableMap<String, String> tuple = getTuple(rs, bindingSet);
                    mutableCopy.remove(tuple);
                    returnedAnswers.add(tuple);
                }
                String errorMessageSuffix = returnedAnswers.size() > 10?
                        "were not returned":
                        "the query returned " +returnedAnswers;

                assertTrue(
                        "The mappings "+ expectedTuples+ " were expected among the answers, but "+ errorMessageSuffix,
                        mutableCopy.isEmpty()
                );
            }
    }

    protected void checkContainsOneOfSetSemanticsWithErrorMessage(String query,
                                                                  ImmutableSet<ImmutableMap<String, String>> expectedTuples)
            throws OWLException {

        try (OWLStatement st = createStatement(); TupleOWLResultSet rs = st.executeSelectQuery(query)) {
            LinkedList<ImmutableMap<String, String>> returnedAnswers = new LinkedList<>();
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                ImmutableMap<String, String> tuple = getTuple(rs, bindingSet);
                returnedAnswers.add(tuple);
                if (expectedTuples.contains(tuple))
                    return;
                String errorMessageSuffix = returnedAnswers.size() > 10 ?
                        "none was returned" :
                        "the query returned " + returnedAnswers;
                fail("One of " + expectedTuples + " was expected among the answers, but " + errorMessageSuffix);
            }
        }
    }

    protected ImmutableMap<String, String> getTuple(TupleOWLResultSet rs, OWLBindingSet bindingSet) throws OWLException {
        ImmutableMap.Builder<String, String> tuple = ImmutableMap.builder();
        for (String variable : rs.getSignature()) {
            tuple.put(variable, getStringForBindingValue(bindingSet.getOWLObject(variable)));
        }
        return tuple.build();
    }

    private String getStringForBindingValue(OWLObject owlObject) {
        if(owlObject instanceof OWLIndividual)
            return owlObject.toString();
        if(owlObject instanceof OWLLiteral) {
            OWLLiteral literal = (OWLLiteral) owlObject;
            return literal.getDatatype().isString() ?
                    literal.getLiteral() :
                    literal.toString();
        }
        throw new UnexpectedBindingTypeException("an instance of "+ OWLIndividual.class + " or "+ OWLLiteral.class+ " is expected");
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

    protected String checkReturnedValuesAndOrderReturnSql(String query, List<String> expectedValues) throws Exception {
        return checkReturnedValues(query, true, true, expectedValues);
    }

    protected String checkReturnedValuesUnorderedReturnSql(String query, List<String> expectedValues) throws Exception {
        return checkReturnedValues(query, false, true, expectedValues);
    }

    protected void checkReturnedValuesAndOrder(List<String> expectedValues, String query) throws Exception {
        checkReturnedValues(query, true, false, expectedValues);
    }

    protected void checkReturnedValuesUnordered(List<String> expectedValues, String query) throws Exception {
        checkReturnedValues(query, false, false, expectedValues);
    }

    private String checkReturnedValues(String query, boolean sameOrder, boolean returnSQL,  List<String> expectedValues)
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
            if(!sameOrder){
                expectedValues = Lists.newArrayList(expectedValues);
                Collections.sort(expectedValues);
                Collections.sort(returnedValues);
            }
            assertEquals(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()), expectedValues, returnedValues);
//        assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
//                returnedValues.equals(expectedValues));
            assertEquals(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size(), i);

            // May be null
            return sql;
        }
    }

    private static class UnexpectedBindingTypeException extends RuntimeException{
        UnexpectedBindingTypeException(String message) {
            super(message);
        }
    }
}
