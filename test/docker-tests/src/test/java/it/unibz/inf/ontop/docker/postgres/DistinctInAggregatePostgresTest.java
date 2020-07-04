package it.unibz.inf.ontop.docker.postgres;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.docker.AbstractDistinctInAggregateTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class DistinctInAggregatePostgresTest extends AbstractDistinctInAggregateTest {

    private static String propertiesFile = "/pgsql/university.properties";

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
        CONNECTION = REASONER.getConnection();
    }

//    @Test
//    public void testCountDistinct() throws Exception {
//        testCount(
//                ImmutableMap.of(
//                        "p", buildAnswerIRI("1"),
//                        "cd", "\"2\"^^xsd:integer"
//                ));
//    }

//    @Test
//    public void testAvgDistinct() throws Exception {
//        testAvg(
//                ImmutableMap.of(
//                        "p",buildAnswerIRI("1"),
//                        "ad", "\"10.5000\"^^xsd:decimal"
//                ));
//    }

//    @Test
//    public void testSumDistinct() throws Exception {
//        testSum(
//                ImmutableMap.of(
//                        "p", buildAnswerIRI("1"),
//                        "sd", "\"21\"^^xsd:integer"
//                ));
//    }

//    @Test
//    public void testGroupConcatDistinct() throws Exception {
//        super.testGroupConcatDistinct();
//    }
}
