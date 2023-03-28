package it.unibz.inf.ontop.docker.lightweight.dremio;

import it.unibz.inf.ontop.docker.lightweight.AbstractCastFunctionsTest;
import it.unibz.inf.ontop.docker.lightweight.DremioLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

//@DremioLightweightTest
public class CastDremioTest extends AbstractCastFunctionsTest {
    private static final String PROPERTIES_FILE = "/books/dremio/books-dremio.properties";
    private static final String OBDA_FILE = "/books/dremio/books-dremio.obda";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }
}
