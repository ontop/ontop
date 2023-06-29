package it.unibz.inf.ontop.docker.db2;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.AbstractDistinctInAggregateTest;
import org.junit.BeforeClass;

public class DistinctInAggregateDB2Test extends AbstractDistinctInAggregateTest {

    private static final String propertiesFile = "/db2/university.properties";

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlFile, obdaFile, propertiesFile);
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForAvg() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1"),
                        "ad", "\"10.5000000000000000000\"^^xsd:decimal"
                ));
    }
}
