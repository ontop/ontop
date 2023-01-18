package it.unibz.inf.ontop.docker.lightweight.postgresql;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

@PostgreSQLLightweightTest
public class DistinctInAggregatePostgreSQLTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-postgresql.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForAvg() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1"),
                        "ad", "\"10.5000000000000000\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("3"),
                        "ad", "\"12.0000000000000000\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("8"),
                        "ad", "\"13.0000000000000000\"^^xsd:decimal"
                ));
    }
}
