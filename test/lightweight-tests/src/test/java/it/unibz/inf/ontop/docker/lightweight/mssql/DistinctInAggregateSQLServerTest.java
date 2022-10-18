package it.unibz.inf.ontop.docker.lightweight.mssql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.MSSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@MSSQLLightweightTest
public class DistinctInAggregateSQLServerTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-mssql.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Test
    @Disabled("STRING_AGG(DISTINCT) is not supported by MSSQL")
    @Override
    public void testGroupConcatDistinct() {
    }

    @Override
    protected ImmutableList<ImmutableMap<String, String>> getTuplesForAvg() {
        return ImmutableList.of(
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
}

