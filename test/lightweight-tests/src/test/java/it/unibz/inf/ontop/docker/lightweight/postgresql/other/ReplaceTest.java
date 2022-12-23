package it.unibz.inf.ontop.docker.lightweight.postgresql.other;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@PostgreSQLLightweightTest
public class ReplaceTest extends AbstractDockerRDF4JTest {

    protected static final String OWL_FILE = "/prof/prof.owl";
    protected static final String OBDA_FILE = "/prof/prof.obda";
    private static final String PROPERTIES_FILE = "/prof/postgresql/prof-postgresql.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testReplace() {
        String query = "select ?v where {\n" +
                "    bind(replace(\n" +
                "            'ABC AA', 'A', 'Z'\n" +
                "        ) as ?v)\n" +
                "}";

        ImmutableList<String> results = runQuery(query);
        Assertions.assertEquals(ImmutableList.of("\"ZBC ZZ\"^^xsd:string"), results);
    }

}
