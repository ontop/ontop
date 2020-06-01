package it.unibz.inf.ontop.docker.mysql;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.docker.service.QuestSPARQLRewriterTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class DistinctInAggregateTest extends AbstractVirtualModeTest {

    static final String owlFile = "/mysql/university/university.ttl";
    static final String obdaFile = "/mysql/university/university.obda";
    static final String propertiesFile = "/mysql/university/university.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
        CONNECTION = REASONER.getConnection();
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
    public void testCountDistinct() throws Exception {
        String query = readQueryFile("/mysql/university/countDistinct.rq");
        assertTrue(
                checkContainsTuplesSetSemantics(
                        query,
                        ImmutableSet.of(
                                ImmutableMap.of(
                                        "p",buildAnswerIRI("1"),
                                        "cd", "\"2\"^^xsd:integer"
                                ))));
    }

    @Test
    public void testAvgDistinct() throws Exception {
        String query = readQueryFile("/mysql/university/avgDistinct.rq");
        assertTrue(
                checkContainsTuplesSetSemantics(
                        query,
                        ImmutableSet.of(
                                ImmutableMap.of(
                                        "p",buildAnswerIRI("1"),
                                        "ad", "\"10.5000\"^^xsd:decimal"
                                ))));
    }

    @Test
    public void testSumDistinct() throws Exception {
        String query = readQueryFile("/mysql/university/sumDistinct.rq");
        assertTrue(
                checkContainsTuplesSetSemantics(
                        query,
                        ImmutableSet.of(
                                ImmutableMap.of(
                                        "p",buildAnswerIRI("1"),
                                        "sd", "\"21\"^^xsd:integer"
                                ))));
    }

    @Test
    public void testGroupConcatDistinct() throws Exception {
        String query = readQueryFile("/mysql/university/groupConcatDistinct.rq");
        assertTrue(
                checkContainsTuplesSetSemantics(
                        query,
                        ImmutableSet.of(
                                ImmutableMap.of(
                                        "p",buildAnswerIRI("1"),
                                        "sd", "\"21\"^^xsd:integer"
                                ))));
    }

    private String buildAnswerIRI(String s) {
        return "<http://www.example.org/test#"+s+">";
    }

    private String readQueryFile(String queryFile) throws IOException {
        Path path = Paths.get(QuestSPARQLRewriterTest.class.getResource(queryFile).getPath());
        return new String(Files.readAllBytes(path));
    }
}
