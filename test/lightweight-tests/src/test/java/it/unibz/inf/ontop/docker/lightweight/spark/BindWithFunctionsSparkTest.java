package it.unibz.inf.ontop.docker.lightweight.spark;

import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.SparkSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@SparkSQLLightweightTest
public class BindWithFunctionsSparkTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/spark/books-spark.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Disabled("Spark SQL does not support OFFSET, Databricks does")
    @Test
    public void testOffset1() {
        super.testOffset1();
    }

    @Disabled("Spark SQL does not support OFFSET, Databricks does")
    @Test
    public void testOffset2() {
        super.testOffset2();
    }
}
