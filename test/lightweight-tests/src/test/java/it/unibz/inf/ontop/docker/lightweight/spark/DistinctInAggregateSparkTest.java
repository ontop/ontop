package it.unibz.inf.ontop.docker.lightweight.spark;

import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.SparkSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@SparkSQLLightweightTest
public class DistinctInAggregateSparkTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-spark.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

}
