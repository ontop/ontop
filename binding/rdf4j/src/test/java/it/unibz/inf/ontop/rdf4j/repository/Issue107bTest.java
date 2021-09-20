package it.unibz.inf.ontop.rdf4j.repository;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class Issue107bTest extends AbstractRDF4JTest {

    private static final String R2RML_FILE = "/issue107b/mapping.ttl";
    private static final String SQL_SCRIPT = "/issue107b/database.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initR2RML(SQL_SCRIPT, R2RML_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    /**
        the issue was fixed in the meta-mapping expander overhaul in April 2020
     */

    @Test
    public void test_metamapping_expansion()  {

        ImmutableSet<ImmutableMap<String, String>> expected = ImmutableSet.of(
                ImmutableMap.of("g", "http://example.org/test",
                        "s", "http://example.org/test/id-1",
                        "p", "http://example.org/S030500",
                        "o", "S030500"));

        assertEquals(expected, ImmutableSet.copyOf(executeQuery(
                "SELECT * WHERE { GRAPH ?g { ?s ?p ?o } }")));
    }
}

