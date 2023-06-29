package it.unibz.inf.ontop.docker.lightweight.dremio;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractNestedDataTest;
import it.unibz.inf.ontop.docker.lightweight.DremioLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.SparkSQLLightweightTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.sql.SQLException;

@DremioLightweightTest
public class NestedDataDremioTest extends AbstractNestedDataTest {

    private static final String PROPERTIES_FILE = "/nested/dremio/nested-dremio.properties";
    private static final String LENS_FILE = "/nested/dremio/nested-lenses-array.json";

    private static final String OBDA_FILE= "/nested/nested-no-pos.obda";

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

    //Since we don't have access to index values, some individuals are treated as equals even though they should not normally be equal.
    @Override
    protected ImmutableMultiset getFlattenWithAggregateExpectedValues() {
        return ImmutableMultiset.of("\"Carl: 17500.0\"^^xsd:string", "\"Jim: 12857.142857142857\"^^xsd:string",
                "\"Cynthia: 12857.142857142857\"^^xsd:string", "\"Sam: 13666.666666666666\"^^xsd:string",
                "\"Bob: 15200.0\"^^xsd:string");
    }

    //Since we don't have access to index values, some individuals are treated as equals even though they should not normally be equal.
    protected ImmutableMultiset getFlatten2DArrayExpectedValues() {
        return ImmutableMultiset.of( "\"Sam\"^^xsd:string", "\"Cynthia\"^^xsd:string", "\"Cynthia\"^^xsd:string", "\"Cynthia\"^^xsd:string", "\"Bob\"^^xsd:string",
                "\"Bob\"^^xsd:string", "\"Jim\"^^xsd:string", "\"Jim\"^^xsd:string", "\"Jim\"^^xsd:string", "\"Carl\"^^xsd:string");
    }

    @Disabled("Currently we do not support flatten with position for Dremio")
    @Test
    @Override
    public void testFlattenWithPosition() throws Exception {
        super.testFlattenWithPosition();
    }

    //Due to the reason mentioned above, we have fewer triples in the VKG.
    @Override
    protected int getSPOExpectedCount() {
        return 76;
    }

    @Override
    protected boolean supportsPositionVariable() {
        return false;
    }
}
