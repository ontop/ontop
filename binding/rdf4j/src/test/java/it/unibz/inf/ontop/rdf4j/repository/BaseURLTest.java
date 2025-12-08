package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;


public class BaseURLTest extends AbstractRDF4JTest {

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initR2RML("/base-url/employee.sql", "/base-url/mapping-relative-url.ttl", null,
                "/base-url/base-url.properties");
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test // Concatenation function
    public void testURL() {
        String query = "PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v foaf:name ?n . \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("http://test.example.org/someBase/1",
                "http://test.example.org/someBase/2"));
    }
}