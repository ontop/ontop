package it.unibz.inf.ontop.docker.lightweight.postgresql;

import it.unibz.inf.ontop.docker.lightweight.AbstractNestedDataTest;
import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@PostgreSQLLightweightTest
public class NestedDataArrayPostgreSQLTest extends AbstractNestedDataTest {

    private static final String PROPERTIES_FILE = "/nested/postgresql/nested-postgresql.properties";
    private static final String OBDA_FILE = "/nested/nested.obda";
    private static final String LENS_FILE = "/nested/postgresql/nested-lenses-array.json";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE, LENS_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }
    
}
