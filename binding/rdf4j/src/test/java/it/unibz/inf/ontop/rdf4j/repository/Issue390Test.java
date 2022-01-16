package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class Issue390Test extends AbstractRDF4JTest {

    private static final String R2RML_FILE = "/issue390/mapping.ttl";
    private static final String SQL_SCRIPT = "/issue390/database.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initR2RML(SQL_SCRIPT, R2RML_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    /**
     *   the issue was fixed in R2RML and OBDA parser overhaul in Nov 2020
     */

    @Test
    public void test_graphs_for_predicate_object_maps() {

        ImmutableSet<ImmutableMap<String, String>> expected = ImmutableSet.of(
                ImmutableMap.of("g", "http://example.org#graph_1",
                        "s", "http://example.org/agency/42",
                        "p", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                        "o", "http://example.org/terms#agency"),
                ImmutableMap.of("g", "http://example.org#graph_1",
                        "s", "http://example.org/agency/42",
                        "p", "http://example.org#has_url",
                        "o", "http://aaa.com"),
                ImmutableMap.of("g", "http://example.org#graph_2",
                        "s", "http://example.org/agency/42",
                        "p", "http://example.org#has_url",
                        "o", "http://aaa.com"));

        assertEquals(expected, ImmutableSet.copyOf(executeQuery("SELECT *\n" +
                "WHERE { GRAPH ?g { ?s ?p ?o } }")));
    }
}
