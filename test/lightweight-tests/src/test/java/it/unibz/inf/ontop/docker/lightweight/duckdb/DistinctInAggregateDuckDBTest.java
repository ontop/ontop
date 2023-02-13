package it.unibz.inf.ontop.docker.lightweight.duckdb;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.AthenaLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@DuckDBLightweightTest
public class DistinctInAggregateDuckDBTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-athena.properties";
    private static final String OBDA_FILE_ATHENA = "/university/university-athena.obda"; //Athena does not support default
                                                                                         //schemas, so we need to provide an
                                                                                         //obda file with fully qualified names.

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE_ATHENA, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForAvg() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1"),
                        "ad", "\"10.5\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("3"),
                        "ad", "\"12.0\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("8"),
                        "ad", "\"13.0\"^^xsd:decimal"
                )
        );
    }

    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForConcat1() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p", buildAnswerIRI("1"),
                        "sd", "\"11|10\"^^xsd:string"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("3"),
                        "sd", "\"12\"^^xsd:string"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("8"),
                        "sd", "\"13\"^^xsd:string"
                ));
    }

    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForConcat2() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p", buildAnswerIRI("1"),
                        "sd", "\"10|11\"^^xsd:string"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("3"),
                        "sd", "\"12\"^^xsd:string"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("8"),
                        "sd", "\"13\"^^xsd:string"
                ));
    }

    @Test
    public void testGroupConcatDistinct() throws Exception {
        ImmutableSet results = executeQueryAndCompareBindingLexicalValues(readQueryFile(groupConcatDistinctQueryFile));
        Assertions.assertTrue(results.equals(getTuplesForConcat1()) || results.equals(getTuplesForConcat2()));
    }
}
