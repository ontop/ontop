package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class RDF4JR2RMLConstantLangTagTest extends AbstractRDF4JTest {

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initR2RML("/prof/prof.sql", "/prof/prof-cst-langtag.mapping.ttl");
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testLangTag1() {
        String query = "PREFIX : <http://example.org/voc#>\n" +
                "SELECT *\n" +
                "WHERE {\n" +
                "  ?p a :Professor ; :label ?v \n" +
                "  FILTER (lang(?v)='it')\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("Professore"));
    }
}
