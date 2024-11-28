package it.unibz.inf.ontop.docker.lightweight.redshift;

import com.google.common.collect.ImmutableMultiset;
import it.unibz.inf.ontop.docker.lightweight.AbstractNestedDataTest;
import it.unibz.inf.ontop.docker.lightweight.RedshiftLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@RedshiftLightweightTest
public class NestedDataRedshiftTest extends AbstractNestedDataTest {

    private static final String PROPERTIES_FILE = "/nested/redshift/nested-redshift.properties";
    private static final String LENS_FILE = "/nested/redshift/nested-lenses-array.json";

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
        return ImmutableMultiset.of( "\"2023-01-01T18:00:00+00:00\"^^xsd:dateTime", "\"2023-01-15T18:00:00+00:00\"^^xsd:dateTime", "\"2023-01-29T12:00:00+00:00\"^^xsd:dateTime",
                "\"2023-02-12T18:00:00+00:00\"^^xsd:dateTime", "\"2023-02-26T18:00:00+00:00\"^^xsd:dateTime",
                "\"2023-03-12T18:00:00+00:00\"^^xsd:dateTime", "\"2023-03-26T18:00:00+00:00\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableMultiset getFlattenWithAggregateExpectedValues() {
        return ImmutableMultiset.of("\"Carl: 15000.000000000000000000\"^^xsd:string", "\"Jim: 15666.666666666666666666\"^^xsd:string",
                "\"Cynthia: 13000.000000000000000000\"^^xsd:string", "\"Sam: 10000.000000000000000000\"^^xsd:string",
                "\"Bob: 17666.666666666666666666\"^^xsd:string");
    }

    //Redshift starts counting at 0 for the position argument.
    @Override
    protected ImmutableMultiset getFlattenWithPositionExpectedValues() {
        return ImmutableMultiset.of( "\"1\"^^xsd:integer", "\"1\"^^xsd:integer", "\"1\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"2\"^^xsd:integer");
    }
}
