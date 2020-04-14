package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class RDF4JMetaMappingTest extends AbstractRDF4JTest {

    private static final String R2RML_FILE = "/meta/meta-mapping.ttl";
    private static final String SQL_SCRIPT = "/meta/meta-create.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initR2RML(SQL_SCRIPT, R2RML_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }
    @Test
    public void testSPO() {
        int count = runQueryAndCount("SELECT * WHERE {?s ?p ?o .}");
        assertEquals(4, count); // VALi :a testClass & VALi :a VALi, for i = 1, 2
    }
}