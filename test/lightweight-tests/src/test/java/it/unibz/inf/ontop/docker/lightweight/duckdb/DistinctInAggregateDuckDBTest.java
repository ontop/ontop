package it.unibz.inf.ontop.docker.lightweight.duckdb;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@DuckDBLightweightTest
public class DistinctInAggregateDuckDBTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-duckdb.properties";
    private static final String OBDA_FILE_DUCKDB = "/university/university-athena.obda"; //DuckDB's JDBC does not support default
                                                                                         //schemas, so we need to provide an
                                                                                         //obda file with fully qualified names.

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE_DUCKDB, OWL_FILE, PROPERTIES_FILE);
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

}
