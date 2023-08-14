package it.unibz.inf.ontop.docker.lightweight.mssql;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.MSSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@MSSQLLightweightTest
public class DistinctInAggregateSQLServerTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-mssql.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Test
    @Disabled("STRING_AGG(DISTINCT) is not supported by MSSQL")
    @Override
    public void testGroupConcatDistinct() {
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForAvg() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1"),
                        "ad", "\"10.500000\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("3"),
                        "ad", "\"12.000000\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("8"),
                        "ad", "\"13.000000\"^^xsd:decimal"
                ));
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForStdev() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1"),
                        "pop", "\"4.496912521077347\"^^xsd:decimal",
                        "samp", "\"5.507570547286101\"^^xsd:decimal",
                        "stdev", "\"5.507570547286101\"^^xsd:decimal"
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
                        "pop", "\"20.222222222222218\"^^xsd:decimal",
                        "samp", "\"30.33333333333333\"^^xsd:decimal",
                        "variance", "\"30.33333333333333\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("3"),
                        "pop", "\"30.25\"^^xsd:decimal",
                        "samp", "\"60.5\"^^xsd:decimal",
                        "variance", "\"60.5\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("8"),
                        "pop", "\"36.0\"^^xsd:decimal",
                        "samp", "\"72.0\"^^xsd:decimal",
                        "variance", "\"72.0\"^^xsd:decimal"
                )
        );
    }
}

