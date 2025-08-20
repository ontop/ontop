package it.unibz.inf.ontop.docker.lightweight.tdengine;

import it.unibz.inf.ontop.docker.lightweight.TDEngineLightWeightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@TDEngineLightWeightTest
public class WebSocketCastTDengineTest extends AbstractCastTDengineTest {
    protected static final String PROPERTIES_FILE = "/tdengine/functions-rs.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, null, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }
}