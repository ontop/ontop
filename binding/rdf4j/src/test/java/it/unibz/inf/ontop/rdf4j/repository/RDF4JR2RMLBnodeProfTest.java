package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.sql.SQLException;

public class RDF4JR2RMLBnodeProfTest extends AbstractRDF4JBnodeProfTest {

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initR2RML("/prof/prof.sql", "/prof/prof-bnode.mapping.ttl");
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }
}
