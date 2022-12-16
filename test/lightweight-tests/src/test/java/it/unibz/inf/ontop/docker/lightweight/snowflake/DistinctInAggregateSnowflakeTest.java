package it.unibz.inf.ontop.docker.lightweight.snowflake;

import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.SnowflakeLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@SnowflakeLightweightTest
public class DistinctInAggregateSnowflakeTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-snowflake.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA("/university/university-snowflake.obda", OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }
}
