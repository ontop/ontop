package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class NonDistinctViewTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/view-non-distinct/mapping.obda";
    private static final String SQL_SCRIPT = "/view-non-distinct/database.sql";
    private static final String VIEW_FILE = "/view-non-distinct/view.json";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, null, VIEW_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    /**
     *   the issue was fixed in R2RML and OBDA parser overhaul in Nov 2020
     */

    @Test
    public void testNoDuplicate() {

        ImmutableList<ImmutableMap<String, String>> expected = ImmutableList.of(
                ImmutableMap.of("r", "http://test.example.org/roles/1"));

        assertEquals(expected, executeQuery("SELECT ?r\n" +
                "WHERE { ?r a <http://test.example.org/Role> }"));
    }
}
