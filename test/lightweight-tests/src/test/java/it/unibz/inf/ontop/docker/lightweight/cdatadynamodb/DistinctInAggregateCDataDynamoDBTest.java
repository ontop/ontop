package it.unibz.inf.ontop.docker.lightweight.cdatadynamodb;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.CDataDynamoDBLightweightTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.SQLException;

@CDataDynamoDBLightweightTest
public class DistinctInAggregateCDataDynamoDBTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-cdatadynamodb.properties";
    private static final String OBDA_FILE = "/university/university-cdatadynamodb.obda";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Disabled("GROUP CONCAT not supported.")
    @Test
    @Override
    public void testGroupConcatDistinct() throws Exception {
        super.testGroupConcatDistinct();
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForAvg() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1.000000"),
                        "ad", "\"10.5\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("3.000000"),
                        "ad", "\"12\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("8.000000"),
                        "ad", "\"13\"^^xsd:decimal"
                )
        );
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForCount() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p", buildAnswerIRI("1.000000"),
                        "cd", "\"2\"^^xsd:integer"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("3.000000"),
                        "cd", "\"1\"^^xsd:integer"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("8.000000"),
                        "cd", "\"1\"^^xsd:integer"
                )
        );
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForSum() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p", buildAnswerIRI("1.000000"),
                        "sd", "\"21\"^^xsd:integer"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("3.000000"),
                        "sd", "\"12\"^^xsd:integer"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("8.000000"),
                        "sd", "\"13\"^^xsd:integer"
                ));
    }
}
