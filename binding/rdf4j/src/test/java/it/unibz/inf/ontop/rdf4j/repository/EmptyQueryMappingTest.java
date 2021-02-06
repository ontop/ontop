package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class EmptyQueryMappingTest extends AbstractRDF4JTest {

    private static final String R2RML_FILE = "/empty-query-mapping/mapping.ttl";
    private static final String SQL_SCRIPT = "/empty-query-mapping/database.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initR2RML(SQL_SCRIPT, R2RML_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void test_mapping_started() {
        assertEquals(1, 1);
    }
}
