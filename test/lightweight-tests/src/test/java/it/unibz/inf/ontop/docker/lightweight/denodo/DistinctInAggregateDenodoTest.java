package it.unibz.inf.ontop.docker.lightweight.denodo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.DenodoLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@DenodoLightweightTest
public class DistinctInAggregateDenodoTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-denodo.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
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

    @Disabled("Denodo does not support 'DISTINCT' within GROUP_CONCAT")
    @Test
    @Override
    public void testGroupConcatDistinct() throws Exception {
        super.testGroupConcatDistinct();
    }

    @Disabled("Distinct statistical functions are not supported by this dialect.")
    @Test
    @Override
    public void testStdevDistinct() throws Exception {
        super.testStdevDistinct();
    }

    @Disabled("Distinct statistical functions are not supported by this dialect.")
    @Test
    @Override
    public void testVarianceDistinct() throws Exception {
        super.testVarianceDistinct();
    }
}
