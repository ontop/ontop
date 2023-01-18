package it.unibz.inf.ontop.docker.postgres;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.AbstractDistinctInAggregateTest;
import org.junit.BeforeClass;

public class DistinctInAggregatePostgresTest extends AbstractDistinctInAggregateTest {

    private static final String propertiesFile = "/pgsql/university.properties";

    @BeforeClass
    public static void before()  {
        CONNECTION = createReasoner(owlFile, obdaFile, propertiesFile);
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
