package it.unibz.inf.ontop.docker.lightweight.mariadb;

import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.MariaDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@MariaDBLightweightTest
public class DistinctInAggregateMariaDBTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-mariadb.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }
}
