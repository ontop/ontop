package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.sql.SQLException;

public class RDF4JOBDABnodeProfTest extends AbstractRDF4JBnodeProfTest {

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA("/prof/prof.sql", "/prof/prof-bnode.obda");
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

}
