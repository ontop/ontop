package it.unibz.inf.ontop.docker.lightweight.snowflake;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.SnowflakeLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

@SnowflakeLightweightTest
public class DistinctInAggregateSnowflakeTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-snowflake.properties";

    @BeforeAll
    public static void before() {
        initOBDA("/university/university-snowflake.obda", OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after()  {
        release();
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForAvg() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1"),
                        "ad", "\"10.500\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("3"),
                        "ad", "\"12.000\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("8"),
                        "ad", "\"13.000\"^^xsd:decimal"
                )
        );
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForStdev() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1"),
                        "pop", "\"4.496912496369037\"^^xsd:decimal",
                        "samp", "\"5.5075705170247256\"^^xsd:decimal",
                        "stdev", "\"5.5075705170247256\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("3"),
                        "pop", "\"5.5\"^^xsd:decimal",
                        "samp", "\"7.7781745930520225\"^^xsd:decimal",
                        "stdev", "\"7.7781745930520225\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("8"),
                        "pop", "\"6.0\"^^xsd:decimal",
                        "samp", "\"8.48528137423857\"^^xsd:decimal",
                        "stdev", "\"8.48528137423857\"^^xsd:decimal"
                )
        );
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForVariance() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p", buildAnswerIRI("1"),
                        "pop", "\"20.222222\"^^xsd:decimal",
                        "samp", "\"30.333333\"^^xsd:decimal",
                        "variance", "\"30.333333\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("3"),
                        "pop", "\"30.250000\"^^xsd:decimal",
                        "samp", "\"60.500000\"^^xsd:decimal",
                        "variance", "\"60.500000\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("8"),
                        "pop", "\"36.000000\"^^xsd:decimal",
                        "samp", "\"72.000000\"^^xsd:decimal",
                        "variance", "\"72.000000\"^^xsd:decimal"
                )
        );
    }
}
