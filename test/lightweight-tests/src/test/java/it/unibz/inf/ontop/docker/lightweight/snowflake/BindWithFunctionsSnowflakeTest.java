package it.unibz.inf.ontop.docker.lightweight.snowflake;

import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.SnowflakeLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@SnowflakeLightweightTest
public class BindWithFunctionsSnowflakeTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/snowflake/books-snowflake.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA("/books/snowflake/books-snowflake.obda", OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }
}
