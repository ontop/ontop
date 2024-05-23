package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;


public class NullDuplicatedColumnTest extends AbstractRDF4JTest {

    private static final String LENS_FILE = "/timestamp/duplicating-col-lenses.json";
    private static final String OBDA_FILE = "/timestamp/prof-duplication-col-lens.obda";
    private static final String SQL_SCRIPT = "/timestamp/prof.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, null, LENS_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testBirthdays() {
        String query = "PREFIX : <http://university.example.org/>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x :birthday_without_timezone ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of());
    }
}
