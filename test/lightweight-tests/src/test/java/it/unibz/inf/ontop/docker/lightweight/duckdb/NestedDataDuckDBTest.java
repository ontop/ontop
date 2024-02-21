package it.unibz.inf.ontop.docker.lightweight.duckdb;

import com.google.common.collect.ImmutableMultiset;
import it.unibz.inf.ontop.docker.lightweight.AbstractNestedDataTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@DuckDBLightweightTest
public class NestedDataDuckDBTest extends AbstractNestedDataTest {

    private static final String PROPERTIES_FILE = "/nested/duckdb/nested-duckdb.properties";
    private static final String LENS_FILE = "/nested/duckdb/nested-lenses-array.json";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE, LENS_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableMultiset getFlattenWithAggregateExpectedValues() {
        return ImmutableMultiset.of("\"Carl: 15000.0\"^^xsd:string", "\"Jim: 15666.666666666666\"^^xsd:string",
                "\"Cynthia: 13000.0\"^^xsd:string", "\"Sam: 10000.0\"^^xsd:string",
                "\"Bob: 17666.666666666668\"^^xsd:string");
    }

}
