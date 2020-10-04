package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;


public class RDF4JLangTest extends AbstractRDF4JTest {

    private static final String CREATE_DB_FILE = "/label_comment.sql";
    private static final String OBDA_FILE = "/label_comment.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testSameLanguageLabelComment() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "  ?o rdfs:label ?label ;\n" +
                "     rdfs:comment ?comment .\n" +
                "  FILTER( LANG(?label) = LANG(?comment) )\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testLangMatches1() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "  ?o rdfs:label ?label .\n" +
                "  FILTER(LANGMATCHES(LANG(?label), \"de\"))\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testExternalBinding1() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?s rdfs:label ?l ; rdfs:comment ?v .\n" +
                "}\n" +
                "ORDER BY ?v";

        ImmutableList<String> results = ImmutableList.of("Deutsche Beschreibung","English description");
        MapBindingSet bindings = new MapBindingSet();
        bindings.addBinding("l", SimpleValueFactory.getInstance().createLiteral("testdata", "en"));

        runQueryAndCompare(query, results, bindings);
    }

    @Test
    public void testExternalBinding2() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?s rdfs:label ?v .\n" +
                "}\n" +
                "ORDER BY ?v";

        ImmutableList<String> results = ImmutableList.of("testdata");
        MapBindingSet bindings = new MapBindingSet();
        bindings.addBinding("v", SimpleValueFactory.getInstance().createLiteral("testdata", "en"));

        runQueryAndCompare(query, results, bindings);
    }

    @Test
    public void testExternalBinding3() {
        String query = "SELECT (CONCAT(?l,\"ssss\") AS ?v) \n" +
                "WHERE {\n" +
                "   ?s rdfs:label ?l .\n" +
                "}\n";

        ImmutableList<String> results = ImmutableList.of("testdatassss");
        MapBindingSet bindings = new MapBindingSet();
        bindings.addBinding("v", SimpleValueFactory.getInstance().createLiteral("testdatassss"));

        runQueryAndCompare(query, results, bindings);
    }

    @Test
    public void testExternalBinding4() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?s rdfs:label ?v .\n" +
                "   BIND(CONCAT(?v,\"ssss\") AS ?r)" +
                "}\n" +
                "ORDER BY ?v";

        ImmutableList<String> results = ImmutableList.of("testdata");
        MapBindingSet bindings = new MapBindingSet();
        bindings.addBinding("r", SimpleValueFactory.getInstance().createLiteral("testdatassss"));

        runQueryAndCompare(query, results, bindings);
    }

    @Test
    public void testExternalBinding5() {
        String query = "SELECT (SUM(?n) AS ?v)\n" +
                "WHERE {\n" +
                "   ?s rdfs:label ?l ; rdfs:comment ?c . \n" +
                "   BIND(IF(langMatches(lang(?l), \"en\"), 1, 0) AS ?n)\n" +
                "}\n" +
                "GROUP BY ?l";

        ImmutableList<String> results = ImmutableList.of("2");
        MapBindingSet bindings = new MapBindingSet();
        bindings.addBinding("v", SimpleValueFactory.getInstance().createLiteral(2));

        runQueryAndCompare(query, results, bindings);
    }

    @Test
    public void testExternalBinding6() {
        String query = "SELECT (?c AS ?v)\n" +
                "WHERE {\n" +
                "   ?s rdfs:label ?l ; rdfs:comment ?c .\n" +
                "}\n" +
                "ORDER BY ?c";

        ImmutableList<String> results = ImmutableList.of("Deutsche Beschreibung","English description");
        MapBindingSet bindings = new MapBindingSet();
        bindings.addBinding("l", SimpleValueFactory.getInstance().createLiteral("testdata", "en"));

        runQueryAndCompare(query, results, bindings);
    }

    @Test
    public void testExternalBinding7() {
        String query = "SELECT (?l AS ?v)\n" +
                "WHERE {\n" +
                "   ?s rdfs:label ?l .\n" +
                "}\n" +
                "ORDER BY ?v";

        ImmutableList<String> results = ImmutableList.of("testdata");
        MapBindingSet bindings = new MapBindingSet();
        bindings.addBinding("v", SimpleValueFactory.getInstance().createLiteral("testdata", "en"));

        runQueryAndCompare(query, results, bindings);
    }

    @Test
    public void testExternalBinding8() {
        String query = "SELECT *\n" +
                "WHERE {\n" +
                "   ?s rdfs:label ?v .\n" +
                "   VALUES (?v ?r) { \n" +
                "      (\"testdata\"@en \"ok\") \n" +
                "      (\"testdaten\"@de \"not\") \n" +
                "    }\n" +
                "}\n" +
                "ORDER BY ?v";

        ImmutableList<String> results = ImmutableList.of("testdata");
        MapBindingSet bindings = new MapBindingSet();
        bindings.addBinding("r", SimpleValueFactory.getInstance().createLiteral("ok"));

        runQueryAndCompare(query, results, bindings);
    }

    @Test
    public void testExternalBindingGraph1() {
        String query = "CONSTRUCT {\n" +
                "  <http://ex.org/21> rdfs:label ?l \n" +
                "} \n" +
                "WHERE {\n" +
                "   ?s rdfs:label ?l ; rdfs:comment ?v .\n" +
                "}\n";

        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        Literal label = valueFactory.createLiteral("testdata", "en");

        ImmutableSet<Statement> expectedValues = ImmutableSet.of(
                valueFactory.createStatement(
                        valueFactory.createIRI("http://ex.org/21"),
                        RDFS.LABEL,
                        label));

        MapBindingSet bindings = new MapBindingSet();
        bindings.addBinding("l", label);

        runGraphQueryAndCompare(query, expectedValues, bindings);
    }
}
