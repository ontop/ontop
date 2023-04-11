package it.unibz.inf.ontop.docker.lightweight.oracle;

import com.google.common.collect.ImmutableMultiset;
import it.unibz.inf.ontop.docker.lightweight.AbstractNestedDataTest;
import it.unibz.inf.ontop.docker.lightweight.OracleLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@OracleLightweightTest
public class NestedDataOracleTest extends AbstractNestedDataTest {

    private static final String PROPERTIES_FILE = "/nested/oracle/nested-oracle.properties";
    private static final String LENS_FILE = "/nested/oracle/nested-lenses.json";

    private static final String OBDA_FILE= "/nested/nested.obda";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE, LENS_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableMultiset getFlattenTimestampExpectedValues() {
        return ImmutableMultiset.of( "\"2023-01-01T18:00:00.000000\"^^xsd:dateTime", "\"2023-01-15T18:00:00.000000\"^^xsd:dateTime", "\"2023-01-29T12:00:00.000000\"^^xsd:dateTime",
                "\"2023-02-12T18:00:00.000000\"^^xsd:dateTime", "\"2023-02-26T18:00:00.000000\"^^xsd:dateTime",
                "\"2023-03-12T18:00:00.000000\"^^xsd:dateTime", "\"2023-03-26T18:00:00.000000\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableMultiset getFlattenWithAggregateExpectedValues() {
        return ImmutableMultiset.of("\"Carl: 15000\"^^xsd:string", "\"Jim: 1.56666666666666666666666666666666666667E04\"^^xsd:string",
                "\"Cynthia: 13000\"^^xsd:string", "\"Sam: 10000\"^^xsd:string",
                "\"Bob: 1.76666666666666666666666666666666666667E04\"^^xsd:string");
    }
}
