package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class DiverseTemplatesTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/diverse-templates/diverse-templates.obda";
    private static final String SQL_SCRIPT = "/diverse-templates/diverse-templates.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void test1() {
        var query = "PREFIX : <http://example.org/>\n" +
                "SELECT * {\n" +
                "?s a :A ;\n " +
                "   ?p ?o .\n" +
                "?o ?p2 ?v\n" +
                "}\n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, ImmutableList.of("5","5", "7", "100"));
    }

    @Test
    public void test2() {
        var query = "PREFIX : <http://example.org/>\n" +
                "SELECT * {\n" +
                "?s a :B ;\n " +
                "   ?p ?o .\n" +
                "?o ?p2 ?v\n" +
                "}\n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, ImmutableList.of("5", "5"));
    }

    @Test
    public void test2bis() {
        var query = "PREFIX : <http://example.org/>\n" +
                "SELECT * {\n" +
                "?s a :B ;\n " +
                "   ?p ?o .\n" +
                "?o ?p2 ?v\n" +
                "}\n    ";

        runQueryAndCompare(query, ImmutableList.of("5", "5"));
    }

    @Test
    public void test3() {
        var query = "PREFIX : <http://example.org/>\n" +
                "SELECT * {\n" +
                "?s a :C ;\n " +
                "   ?p ?o .\n" +
                "?o ?p2 ?v\n" +
                "}\n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, ImmutableList.of("5","5", "7", "100"));
    }

    @Test
    public void test4() {
        var query = "PREFIX : <http://example.org/>\n" +
                "SELECT * {\n" +
                "BIND (<http://example.org/tpl1/1/2> AS ?s)\n" +
                "?s a :C ;\n " +
                "   ?p ?o .\n" +
                "?o ?p2 ?v\n" +
                "}\n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, ImmutableList.of("5","5"));
    }

    @Test
    public void test5() {
        var query = "PREFIX : <http://example.org/>\n" +
                "SELECT * {\n" +
                "?v a :C ;\n " +
                "   :p2 ?o .\n" +
                "?o ?p2 5\n" +
                "}\n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, ImmutableList.of("http://example.org/tpl1/1/2"));
    }

    @Test
    public void test6() {
        var query = "PREFIX : <http://example.org/>\n" +
                "SELECT * {\n" +
                "?v a :C ;\n " +
                "   ?p ?o .\n" +
                "?o ?p2 5\n" +
                "}\n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, ImmutableList.of("http://example.org/tpl1/1/2", "http://example.org/tpl1/1/2"));
    }

    @Test
    public void test7() {
        var query = "PREFIX : <http://example.org/>\n" +
                "SELECT * {\n" +
                "?v a :C ;\n " +
                "   ?p ?o .\n" +
                "?o :value 5\n" +
                "}\n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, ImmutableList.of("http://example.org/tpl1/1/2", "http://example.org/tpl1/1/2"));
    }

    @Test
    public void test8() {
        var query = "PREFIX : <http://example.org/>\n" +
                "SELECT * {\n" +
                "BIND (<http://example.org/tpl1/2/2> AS ?s)\n" +
                "?s a :C ;\n " +
                "   ?p ?o .\n" +
                "?o ?p2 ?v\n" +
                "}\n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, ImmutableList.of("100"));
    }
}
