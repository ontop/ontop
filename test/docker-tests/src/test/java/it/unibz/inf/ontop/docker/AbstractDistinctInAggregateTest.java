package it.unibz.inf.ontop.docker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.service.QuestSPARQLRewriterTest;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractDistinctInAggregateTest {

    protected static OntopOWLReasoner REASONER;
    protected static OntopOWLConnection CONNECTION;

    protected static final String owlFile = "/distinctInAggregates/university.ttl";
    protected static final String obdaFile = "/distinctInAggregates/university.obda";

    protected static final String sumDistinctQueryFile = "/distinctInAggregates/sumDistinct.rq";
    protected static final String avgDistinctQueryFile = "/distinctInAggregates/avgDistinct.rq";
    protected static final String countDistinctQueryFile = "/distinctInAggregates/countDistinct.rq";
    protected static final String groupConcatDistinctQueryFile = "/distinctInAggregates/groupConcatDistinct.rq";

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



    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    protected void checkContainsTuplesSetSemantics(String query,
                                                      ImmutableSet<ImmutableMap<String, String>> expectedAnswers)
            throws OWLException {
        try (OWLStatement st = createStatement(); TupleOWLResultSet rs = st.executeSelectQuery(query)) {
            Set<ImmutableMap<String, String>> mutableCopy = new HashSet<>(expectedAnswers);
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
                    "The mappings "+ expectedAnswers+ " were expected among the answers, but "+ errorMessageSuffix,
                    mutableCopy.isEmpty()
            );
        }
    }

    protected ImmutableMap<String, String> getTuple(TupleOWLResultSet rs, OWLBindingSet bindingSet) throws OWLException {
        ImmutableMap.Builder<String, String> tuple = ImmutableMap.builder();
        for (String variable : rs.getSignature()) {
            tuple.put(variable, bindingSet.getOWLObject(variable).toString());
        }
        return tuple.build();
    }

    private void runTest(String query, ImmutableMap<String,String> expectedTuple) throws Exception {
                checkContainsTuplesSetSemantics(
                        query,
                        ImmutableSet.of(
                                expectedTuple
                                ));
    }

    protected void testCount(ImmutableMap<String, String> tuple) throws Exception {
                runTest(
                        readQueryFile(countDistinctQueryFile),
                        tuple
        );
    }

    protected void testAvg(ImmutableMap<String, String> tuple) throws Exception {
        runTest(
                readQueryFile(avgDistinctQueryFile),
                tuple
        );
    }

    protected void testSum(ImmutableMap<String, String> tuple) throws Exception {
        runTest(
                readQueryFile(sumDistinctQueryFile),
                tuple
        );
    }

    protected void testGroupConcat(ImmutableMap<String, String> tuple) throws Exception {
        runTest(
                readQueryFile(groupConcatDistinctQueryFile),
                tuple
        );
    }

    protected String buildAnswerIRI(String s) {
        return "<http://www.example.org/test#"+s+">";
    }

    protected String readQueryFile(String queryFile) throws IOException {
        Path path = Paths.get(QuestSPARQLRewriterTest.class.getResource(queryFile).getPath());
        return new String(Files.readAllBytes(path));
    }
}
