package it.unibz.inf.ontop.docker.lightweight.trino;

import com.google.common.collect.ImmutableMultiset;
import it.unibz.inf.ontop.docker.lightweight.AbstractNestedDataTest;
import it.unibz.inf.ontop.docker.lightweight.SparkSQLLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.TrinoLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@TrinoLightweightTest
public class NestedDataTrinoTest extends AbstractNestedDataTest {

    private static final String PROPERTIES_FILE = "/nested/trino/nested-trino.properties";
    private static final String OBDA_FILE = "/nested/nested.obda";
    private static final String LENS_FILE = "/nested/trino/nested-lenses-array.json";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE, LENS_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableMultiset<String> getFlattenTimestampExpectedValues() {
        return ImmutableMultiset.of( "\"2023-01-01 18:00:00.000000\"^^xsd:dateTime", "\"2023-01-15 18:00:00.000000\"^^xsd:dateTime", "\"2023-01-29 12:00:00.000000\"^^xsd:dateTime",
                "\"2023-02-12 18:00:00.000000\"^^xsd:dateTime", "\"2023-02-26 18:00:00.000000\"^^xsd:dateTime",
                "\"2023-03-12 18:00:00.000000\"^^xsd:dateTime", "\"2023-03-26 18:00:00.000000\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableMultiset<String> getFlattenWithAggregateExpectedValues() {
        return ImmutableMultiset.of("\"Carl: 15000.0\"^^xsd:string", "\"Jim: 15666.666666666666\"^^xsd:string",
                "\"Cynthia: 13000.0\"^^xsd:string", "\"Sam: 10000.0\"^^xsd:string",
                "\"Bob: 17666.666666666668\"^^xsd:string");
    }
}
