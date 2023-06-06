package it.unibz.inf.ontop.docker.lightweight.mssql;

import com.google.common.collect.ImmutableMultiset;
import it.unibz.inf.ontop.docker.lightweight.AbstractNestedDataTest;
import it.unibz.inf.ontop.docker.lightweight.MSSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@MSSQLLightweightTest
public class NestedDataSQLServerTest extends AbstractNestedDataTest {

    private static final String PROPERTIES_FILE = "/nested/mssql/nested-mssql.properties";
    private static final String OBDA_FILE = "/nested/nested-no-pos.obda";
    private static final String LENS_FILE = "/nested/mssql/nested-lenses.json";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE, LENS_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    //Since we don't have access to index values, some individuals are treated as equals even though they should not normally be equal.
    @Override
    protected ImmutableMultiset getFlattenWithAggregateExpectedValues() {
        return ImmutableMultiset.of("\"Carl: 17500.000000\"^^xsd:string", "\"Jim: 12857.142857\"^^xsd:string",
                "\"Cynthia: 12857.142857\"^^xsd:string", "\"Sam: 13666.666666\"^^xsd:string",
                "\"Bob: 15200.000000\"^^xsd:string");
    }

    //Since we don't have access to index values, some individuals are treated as equals even though they should not normally be equal.
    protected ImmutableMultiset getFlatten2DArrayExpectedValues() {
        return ImmutableMultiset.of( "\"Sam\"^^xsd:string", "\"Cynthia\"^^xsd:string", "\"Cynthia\"^^xsd:string", "\"Cynthia\"^^xsd:string", "\"Bob\"^^xsd:string",
                "\"Bob\"^^xsd:string", "\"Jim\"^^xsd:string", "\"Jim\"^^xsd:string", "\"Jim\"^^xsd:string", "\"Carl\"^^xsd:string");
    }

    @Disabled("We currently do not support FLATTEN with position for SQLServer")
    @Test
    @Override
    public void testFlattenWithPosition() throws Exception {
        super.testFlattenWithPosition();
    }

    //Due to the reasons mentioned above, we have fewer triples in the KG.
    @Override
    protected int getSPOExpectedCount() {
        return 76;
    }

    @Override
    protected boolean supportsPositionVariable() {
        return false;
    }
}
