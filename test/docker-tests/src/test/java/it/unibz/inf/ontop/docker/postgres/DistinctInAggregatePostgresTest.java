package it.unibz.inf.ontop.docker.postgres;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.AbstractDistinctInAggregateTest;
import org.junit.BeforeClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class DistinctInAggregatePostgresTest extends AbstractDistinctInAggregateTest {

    private static String propertiesFile = "/pgsql/university.properties";

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForAvg() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1"),
                        "ad", "\"10.5000000000000000\"^^xsd:decimal"
                ));
    }
}
