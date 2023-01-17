package it.unibz.inf.ontop.docker.lightweight.spark;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.SparkSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@SparkSQLLightweightTest
public class LeftJoinProfSparkSQLTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/spark/prof-spark.properties";
    private static final String OBDA_FILE = "/prof/spark/prof-spark.obda";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }
}
