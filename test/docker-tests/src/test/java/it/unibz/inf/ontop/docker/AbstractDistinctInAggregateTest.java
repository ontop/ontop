package it.unibz.inf.ontop.docker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.service.QuestSPARQLRewriterTest;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractDistinctInAggregateTest extends AbstractVirtualModeTest {

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

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Test
    public void testGroupConcatDistinct() throws Exception {
        checkContainsOneOfSetSemanticsWithErrorMessage(
                readQueryFile(groupConcatDistinctQueryFile),
                getTuplesForConcat()
        );
    }

    @Test
    public void testSumDistinct() throws Exception {
        checkContainsAllSetSemanticsWithErrorMessage(
                readQueryFile(sumDistinctQueryFile),
                getTuplesForSum()
        );
    }

    @Test
    public void testAvgDistinct() throws Exception {
        checkContainsAllSetSemanticsWithErrorMessage(
                readQueryFile(avgDistinctQueryFile),
                getTuplesForAvg()
        );
    }

    @Test
    public void testCountDistinct() throws Exception {
        checkContainsAllSetSemanticsWithErrorMessage(
                readQueryFile(countDistinctQueryFile),
                getTuplesForCount());
    }

    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForCount() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p", buildAnswerIRI("1"),
                        "cd", "\"2\"^^xsd:integer"
                ));
    }

    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForSum() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p", buildAnswerIRI("1"),
                        "sd", "\"21\"^^xsd:integer"
                ));
    }

    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForAvg() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1"),
                        "ad", "\"10.5000\"^^xsd:decimal"
                ));
    }

    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForConcat() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p", buildAnswerIRI("1"),
                        "sd", "10|11"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("1"),
                        "sd", "11|10"
                ));
    }

    protected String buildAnswerIRI(String s) {
        return "<http://www.example.org/test#"+s+">";
    }

    private String readQueryFile(String queryFile) throws IOException {
        Path path = Paths.get(QuestSPARQLRewriterTest.class.getResource(queryFile).getPath());
        return new String(Files.readAllBytes(path));
    }

}
