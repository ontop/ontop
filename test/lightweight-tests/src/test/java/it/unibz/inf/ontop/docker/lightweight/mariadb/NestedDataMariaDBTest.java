package it.unibz.inf.ontop.docker.lightweight.mariadb;

import com.google.common.collect.ImmutableMultiset;
import it.unibz.inf.ontop.docker.lightweight.AbstractNestedDataTest;
import it.unibz.inf.ontop.docker.lightweight.MariaDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@MariaDBLightweightTest
public class NestedDataMariaDBTest extends AbstractNestedDataTest {

    private static final String PROPERTIES_FILE = "/nested/mariadb/nested-mariadb.properties";
    private static final String OBDA_FILE = "/nested/nested.obda";
    private static final String LENS_FILE = "/nested/mariadb/nested-lenses.json";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE, LENS_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableMultiset<String> getFlattenWithAggregateExpectedValues() {
        return ImmutableMultiset.of("\"Carl: 15000.0000\"^^xsd:string", "\"Jim: 15666.6667\"^^xsd:string",
                "\"Cynthia: 13000.0000\"^^xsd:string", "\"Sam: 10000.0000\"^^xsd:string",
                "\"Bob: 17666.6667\"^^xsd:string");
    }
}
