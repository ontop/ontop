package it.unibz.inf.ontop.docker.lightweight.postgresql;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import org.junit.jupiter.api.BeforeAll;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

@PostgreSQLLightweightTest
public class DistinctInAggregatePostgreSQLTest extends AbstractDistinctInAggregateTest {

    private static String propertiesFile = "/university/university-postgresql.properties";

    @BeforeAll
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
