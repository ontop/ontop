package it.unibz.inf.ontop.docker.lightweight;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractManifestSuiteTest extends AbstractDockerRDF4JTest {

    protected static final String OBDA_FILE = "/stockexchange/stockexchange.obda";
    protected static final String OWL_FILE = "/stockexchange/stockexchange.owl";

    @Test
    public void testAsk() {
        String query = "ASK { \n" +
                "?v <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.owl-ontologies.com/Ontology1207768242.owl#StockBroker> .}";
        boolean val = executeAskQuery(query);
        assertTrue(val);
    }

    @Test
    public void testLiteral1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :stockDescription \" Text description 1 \" .\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testLiteral2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :stockDescription \"Text description 1\"@en-US .\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testString1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :inCity \"Bolzano\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testString2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :inCity Bolzano . \n" +
                "}";
        assertThrows(MalformedQueryException.class, () -> runQuery(query));
    }

    @Test
    public void testString3() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :inCity \"Bolzano\"^^<http://www.w3.org/2001/XMLSchema#string> . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testInteger1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :hasNumber \"3\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testInteger2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :hasNumber 3 . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testInteger3() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :hasNumber \"3\"^^<http://www.w3.org/2001/XMLSchema#integer> . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testPosInteger1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :hasNumber \"+3\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testPosInteger2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :hasNumber +3 . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testPosInteger3() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :hasNumber \"+3\"^^<http://www.w3.org/2001/XMLSchema#integer> . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testNegInteger1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :hasNumber \"-3\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testNegInteger2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :hasNumber -3 . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testNegInteger3() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :inStreet ?street; \n" +
                "   :hasNumber \"-3\" . \n" +
                "}";

        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testDecimal1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionDate ?date; \n" +
                "   :amountOfTransaction \"12.6\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testDecimal2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionDate ?date; \n" +
                "   :amountOfTransaction 12.6 . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testDecimal3() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionDate ?date; \n" +
                "   :amountOfTransaction \"12.6\"^^<http://www.w3.org/2001/XMLSchema#decimal> . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testPosDecimal1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionDate ?date; \n" +
                "   :amountOfTransaction \"+1667.0092\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testPosDecimal2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionDate ?date; \n" +
                "   :amountOfTransaction ?n . \n" +
                "FILTER (?n = +1667.0092) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testPosDecimal3() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionDate ?date; \n" +
                "   :amountOfTransaction ?n . \n" +
                "FILTER (?n = \"+1667.0092\"^^<http://www.w3.org/2001/XMLSchema#decimal>) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testNegDecimal1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionDate ?date; \n" +
                "   :amountOfTransaction \"-2.349\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testNegDecimal2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionDate ?date; \n" +
                "   :amountOfTransaction ?n . \n" +
                "FILTER (?n = -2.349) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testNegDecimal3() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionDate ?date; \n" +
                "   :amountOfTransaction ?n . \n" +
                "FILTER (?n = \"-2.349\"^^<http://www.w3.org/2001/XMLSchema#decimal>) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testDouble1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                "   :companyName ?name; \n" +
                "   :netWorth \"1.2345678e+03\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testDouble2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                "   :companyName ?name; \n" +
                "   :netWorth ?n . \n" +
                "FILTER (?n = 1.2345678e+03) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testDouble3() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                "   :companyName ?name; \n" +
                "   :netWorth ?n . \n" +
                "FILTER (?n = \"1.2345678e+03\"^^<http://www.w3.org/2001/XMLSchema#double>) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testPosDouble1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                "   :companyName ?name; \n" +
                "   :netWorth \"+1.2345678e+03\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testPosDouble2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                "   :companyName ?name; \n" +
                "   :netWorth ?n . \n" +
                "FILTER (?n = +1.2345678e+03) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testPosDouble3() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                "   :companyName ?name; \n" +
                "   :netWorth ?n . \n" +
                "FILTER (?n = \"+1.2345678e+03\"^^<http://www.w3.org/2001/XMLSchema#double>) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testNegDouble1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                "   :companyName ?name; \n" +
                "   :netWorth \"-1.2345678e+03\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testNegDouble2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                "   :companyName ?name; \n" +
                "   :netWorth ?n . \n" +
                "FILTER (?n = -1.2345678e+03) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testNegDouble3() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                "   :companyName ?name; \n" +
                "   :netWorth ?n . \n" +
                "FILTER (?n = \"-1.2345678e+03\"^^<http://www.w3.org/2001/XMLSchema#double>) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testDatetime1a() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate \"2008-04-02\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testDatetime1b() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate \"2008-04-02 00:00:00.0\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testDatetime1c() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate \"2008-04-02T00:00:00Z\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testDatetime2a() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate 2008-04-02 . \n" +
                "}";
        assertThrows(MalformedQueryException.class, () -> runQuery(query));
    }

    @Test
    public void testDatetime2b() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate 2008-04-02 00:00:00.0 . \n" +
                "}";
        assertThrows(MalformedQueryException.class, () -> runQuery(query));
    }

    @Test
    public void testDatetime2c() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate 2008-04-02T00:00:00 . \n" +
                "}";
        assertThrows(MalformedQueryException.class, () -> runQuery(query));
    }

    @Test
    public void testDatetime3a() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate ?d .\n" +
                "FILTER(?d = \"2008-04-02\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";
        assertThrows(QueryEvaluationException.class, () -> runQuery(query));
    }

    @Test
    public void testDatetime3b() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate ?d .\n" +
                "FILTER(?d = \"2008-04-02 00:00:00.0\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";
        assertThrows(QueryEvaluationException.class, () -> runQuery(query));
    }

    @Test
    public void testDatetime3c() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate ?d .\n" +
                "FILTER(?d = \"2008-04-02T00:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testDatetime3d() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate ?d .\n" +
                "FILTER(?d = \"2008-04-02T00:00:00Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testDatetime3e() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate ?d .\n" +
                "FILTER(?d = \"2008-04-02T00:00:00-06:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testDatetime3f() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate ?d .\n" +
                "FILTER(?d = \"2008-04-02T00:00:00-0600\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";
        assertThrows(QueryEvaluationException.class, () -> runQuery(query));
    }

    @Test
    public void testDatetime3g() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate ?d .\n" +
                "FILTER(?d = \"2008-04-02T00:00:00-06\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";
        assertThrows(QueryEvaluationException.class, () -> runQuery(query));
    }

    @Test
    public void testDatetime3h() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate ?d .\n" +
                "FILTER(?d = \"2008-04-02T00:00:00+06:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testDatetime3i() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate ?d .\n" +
                "FILTER(?d = \"2008-04-02T00:00:00+0600\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";
        assertThrows(QueryEvaluationException.class, () -> runQuery(query));
    }

    @Test
    public void testDatetime3j() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :transactionID ?id; \n" +
                "   :transactionDate ?d .\n" +
                "FILTER(?d = \"2008-04-02T00:00:00+06\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";
        assertThrows(QueryEvaluationException.class, () -> runQuery(query));
    }

    @Test
    public void testBoolean1a() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares \"true\" \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testBoolean1b() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares \"TRUE\" \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testBoolean1c() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares \"t\" \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testBoolean1d() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares \"1\" \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testBoolean2a() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares true \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(5, count);
    }

    @Test
    public void testBoolean2b() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares TRUE \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(5, count);
    }

    @Test
    public void testBoolean2c() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares t \n" +
                "}";
        assertThrows(MalformedQueryException.class, () -> runQuery(query));
    }

    @Test
    public void testBoolean2d() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares 1 \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testBoolean3a() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(5, count);
    }

    @Test
    public void testBoolean3b() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares \"TRUE\"^^<http://www.w3.org/2001/XMLSchema#boolean> \n" +
                "}";
        assertThrows(QueryEvaluationException.class, () -> runQuery(query));
    }

    @Test
    public void testBoolean3c() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares \"t\"^^<http://www.w3.org/2001/XMLSchema#boolean> \n" +
                "}";
        assertThrows(QueryEvaluationException.class, () -> runQuery(query));
    }

    @Test
    public void testBoolean3d() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                "   :amountOfShares ?amount; \n" +
                "   :typeOfShares \"1\"^^<http://www.w3.org/2001/XMLSchema#boolean> \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testBooleanAnd() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :addressID ?id; \n" +
                "   :inStreet ?street; \n" +
                "   :inCity ?city; \n" +
                "   :inCountry ?country; \n" +
                "   :inState ?state; \n" +
                "   :hasNumber ?number .\n" +
                "FILTER ( str(?street) = \"Via Marconi\" && str(?city) = \"Bolzano\" ). \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testBooleanOr() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :addressID ?id; \n" +
                "   :inStreet ?street; \n" +
                "   :inCity ?city; \n" +
                "   :inCountry ?country; \n" +
                "   :inState ?state; \n" +
                "   :hasNumber ?number .\n" +
                "FILTER ( str(?street) = \"Via Marconi\" || str(?city) = \"Bolzano\" ). \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testBooleanNested1() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :addressID ?id; \n" +
                "   :inStreet ?street; \n" +
                "   :inCity ?city; \n" +
                "   :inCountry ?country; \n" +
                "   :inState ?state; \n" +
                "   :hasNumber ?number .\n" +
                "FILTER ( str(?street) = \"Via Marconi\" && " +
                "  ( str(?city) = \"Bolzano\" || str(?street) = \"Romer Street\" )). \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testBooleanNested2() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :addressID ?id; \n" +
                "   :inStreet ?street; \n" +
                "   :inCity ?city; \n" +
                "   :inCountry ?country; \n" +
                "   :inState ?state; \n" +
                "   :hasNumber ?number .\n" +
                "FILTER ( str(?country) = \"Italy\" || " +
                "  ( str(?street) = \"Via Marconi\" && " +
                "    ( str(?city) = \"Bolzano\" || str(?street) = \"Romer Street\" ) ) ). \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(3, count);
    }

    @Test
    public void testBooleanNested3() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                "   :addressID ?id; \n" +
                "   :inStreet ?street; \n" +
                "   :inCity ?city; \n" +
                "   :inCountry ?country; \n" +
                "   :inState ?state; \n" +
                "   :hasNumber ?number . \n" +
                "FILTER ( ( str(?country) = \"Italy\" && ?number = 3 ) || " +
                "  ( str(?street) = \"Via Marconi\" " +
                "    && ( str(?city) = \"Bolzano\" || str(?street) = \"Romer Street\" ) ) ). \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testStringEq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city; \n" +
                "FILTER ( str(?city) = \"Bolzano\" ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testStringNeq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city . \n" +
                "FILTER ( str(?city) != \"Bolzano\" ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(5, count);
    }

    @Test
    public void testIntegerEq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :inStreet ?street; \n" +
                " :hasNumber ?number . \n" +
                "FILTER ( ?number = 3 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testIntegerNeq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :inStreet ?street; \n" +
                " :hasNumber ?number . \n" +
                "FILTER ( ?number != 3 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(5, count);
    }

    @Test
    public void testIntegerGt() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :inStreet ?street; \n" +
                " :hasNumber ?number . \n" +
                "FILTER ( ?number > 3 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(5, count);
    }

    @Test
    public void testIntegerGte() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :inStreet ?street; \n" +
                " :hasNumber ?number . \n" +
                "FILTER ( ?number >= 3 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(7, count);
    }

    @Test
    public void testIntegerLt() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :inStreet ?street; \n" +
                " :hasNumber ?number . \n" +
                "FILTER ( ?number < 3 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testIntegerLte() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :inStreet ?street; \n" +
                " :hasNumber ?number . \n" +
                "FILTER ( ?number <= 3 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testDecimalEq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionDate ?date; \n" +
                " :amountOfTransaction ?amount . \n" +
                "FILTER ( ?amount = 12.6 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testDecimalNeq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionDate ?date; \n" +
                " :amountOfTransaction ?amount . \n" +
                "FILTER ( ?amount != 12.6 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(3, count);
    }

    @Test
    public void testDecimalGt() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionDate ?date; \n" +
                " :amountOfTransaction ?amount . \n" +
                "FILTER ( ?amount > 12.6 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testDecimalGte() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionDate ?date; \n" +
                " :amountOfTransaction ?amount . \n" +
                "FILTER ( ?amount >= 12.6 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(3, count);
    }

    @Test
    public void testDecimalLt() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionDate ?date; \n" +
                " :amountOfTransaction ?amount . \n" +
                "FILTER ( ?amount < 12.6 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testDecimalLte() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?date\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionDate ?date; \n" +
                " :amountOfTransaction ?amount . \n" +
                "FILTER ( ?amount <= 12.6 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testDoubleEq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                " :companyName ?name; \n" +
                " :netWorth ?netWorth . \n" +
                "FILTER ( ?netWorth = 1.2345678e+03 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testDoubleNeq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                " :companyName ?name; \n" +
                " :netWorth ?netWorth . \n" +
                "FILTER ( ?netWorth != 1.2345678e+03 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testDoubleGt() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                " :companyName ?name; \n" +
                " :netWorth ?netWorth . \n" +
                "FILTER ( ?netWorth > 1.2345678e+03 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testDoubleGte() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                " :companyName ?name; \n" +
                " :netWorth ?netWorth . \n" +
                "FILTER ( ?netWorth >= 1.2345678e+03 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testDoubleLt() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                " :companyName ?name; \n" +
                " :netWorth ?netWorth . \n" +
                "FILTER ( ?netWorth < 1.2345678e+03 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testDoubleLte() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?name\n" +
                "WHERE {\n" +
                "?v a :Company; \n" +
                " :companyName ?name; \n" +
                " :netWorth ?netWorth . \n" +
                "FILTER ( ?netWorth <= 1.2345678e+03 ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testDatetimeEq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionID ?id; \n" +
                " :transactionDate ?transDate . \n" +
                "FILTER ( ?transDate = \"2008-04-02T00:00:00.0\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testDatetimeNeq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionID ?id; \n" +
                " :transactionDate ?transDate . \n" +
                "FILTER ( ?transDate != \"2008-04-02T00:00:00.0\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(3, count);
    }

    @Test
    public void testDatetimeGt() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionID ?id; \n" +
                " :transactionDate ?transDate . \n" +
                "FILTER ( ?transDate > \"2008-04-02T00:00:00.0\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testDatetimeGte() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionID ?id; \n" +
                " :transactionDate ?transDate . \n" +
                "FILTER ( ?transDate >= \"2008-04-02T00:00:00.0\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(3, count);
    }

    @Test
    public void testDatetimeLt() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionID ?id; \n" +
                " :transactionDate ?transDate . \n" +
                "FILTER ( ?transDate < \"2008-04-02T00:00:00.0\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testDatetimeLte() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                " :transactionID ?id; \n" +
                " :transactionDate ?transDate . \n" +
                "FILTER ( ?transDate <= \"2008-04-02T00:00:00.0\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testBooleanEq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                " :amountOfShares ?amount; \n" +
                " :typeOfShares ?type . \n" +
                "FILTER ( ?type = true ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(5, count);
    }

    @Test
    public void testBooleanNeq() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                " :amountOfShares ?amount; \n" +
                " :typeOfShares ?type . \n" +
                "FILTER ( ?type != true ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(5, count);
    }

    @Test
    public void testLiteralLangmatch() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?amount ?description\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                " :amountOfShares ?amount; \n" +
                " :stockDescription ?description . \n" +
                "FILTER langMatches ( lang(?description), \"en-US\" ) . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(10, count);
    }

    @Test
    public void testSlice() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :addressID ?id; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city; \n" +
                " :inCountry ?country; \n" +
                " :inState ?state; \n" +
                " :hasNumber ?number . \n" +
                "} \n" +
                "LIMIT 3 \n" +
                "OFFSET 2";
        int count = runQueryAndCount(query);
        assertEquals(3, count);
    }

    @Test
    public void testLimit() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :addressID ?id; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city; \n" +
                " :inCountry ?country; \n" +
                " :inState ?state; \n" +
                " :hasNumber ?number . \n" +
                "} \n" +
                "LIMIT 3";
        int count = runQueryAndCount(query);
        assertEquals(3, count);
    }

    @Test
    public void testOffset() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :addressID ?id; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city; \n" +
                " :inCountry ?country; \n" +
                " :inState ?state; \n" +
                " :hasNumber ?number . \n" +
                "} \n" +
                "OFFSET 2";
        int count = runQueryAndCount(query);
        assertEquals(5, count);
    }

    @Test
    public void testLimit0Offsetn() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :addressID ?id; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city; \n" +
                " :inCountry ?country; \n" +
                " :inState ?state; \n" +
                " :hasNumber ?number . \n" +
                "} \n" +
                "LIMIT 0\n" +
                "OFFSET 2";
        int count = runQueryAndCount(query);
        assertEquals(0, count);
    }

    @Test
    public void testLimitnOffset0() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :addressID ?id; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city; \n" +
                " :inCountry ?country; \n" +
                " :inState ?state; \n" +
                " :hasNumber ?number . \n" +
                "} \n" +
                "LIMIT 3\n" +
                "OFFSET 0";
        int count = runQueryAndCount(query);
        assertEquals(3, count);
    }

    //TODO: Test does not make sense
    @Test
    public void testOrderBy() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :addressID ?id; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city; \n" +
                " :inCountry ?country; \n" +
                " :inState ?state; \n" +
                " :hasNumber ?number . \n" +
                "} \n" +
                "ORDER BY ?city";
        int count = runQueryAndCount(query);
        assertEquals(7, count);
    }

    @Test
    public void testOrderByDesc() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :addressID ?id; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city; \n" +
                " :inCountry ?country; \n" +
                " :inState ?state; \n" +
                " :hasNumber ?number . \n" +
                "} \n" +
                "ORDER BY DESC(?city)";
        int count = runQueryAndCount(query);
        assertEquals(7, count);
    }

    @Test
    public void testOrderByCombined() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :addressID ?id; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city; \n" +
                " :inCountry ?country; \n" +
                " :inState ?state; \n" +
                " :hasNumber ?number . \n" +
                "} \n" +
                "ORDER BY ?city DESC(?country)";
        int count = runQueryAndCount(query);
        assertEquals(7, count);
    }

    @Test
    public void testSliceOrderBy() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :addressID ?id; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city; \n" +
                " :inCountry ?country; \n" +
                " :inState ?state; \n" +
                " :hasNumber ?number . \n" +
                "} \n" +
                "ORDER BY ?city \n" +
                "LIMIT 3 \n" +
                "OFFSET 2";
        int count = runQueryAndCount(query);
        assertEquals(3, count);
    }

    @Test
    public void testOrderByLiteral() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Stock; \n" +
                " :financialInstrumentID ?id; \n" +
                " :amountOfShares ?numberofshares; \n" +
                " :typeOfShares ?sharetype; \n" +
                " :stockDescription ?description . \n" +
                "} \n" +
                "ORDER BY ?description";
        int count = runQueryAndCount(query);
        assertEquals(10, count);
    }

    @Test
    public void testAddresses() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :addressID ?id; \n" +
                " :inStreet ?street; \n" +
                " :inCity ?city; \n" +
                " :inCountry ?country; \n" +
                " :inState ?state; \n" +
                " :hasNumber ?number . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(7, count);
    }

    @Test
    public void testAddressesId() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Address; \n" +
                " :addressID ?id; \n" +
                " :inStreet \"Via Marconi\"^^<http://www.w3.org/2001/XMLSchema#string> ; \n" +
                " :inCity \"Bolzano\"^^<http://www.w3.org/2001/XMLSchema#string> ; \n" +
                " :inCountry ?country; \n" +
                " :inState ?state; \n" +
                " :hasNumber ?number . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testPersonAddresses() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                "?v a :Person . \n" +
                "?v :firstName ?fn . \n" +
                "?v :lastName ?ln . \n" +
                "?v :hasAddress ?address . \n" +
                "?address :inStreet ?street . \n" +
                "?address :inCity ?city . \n" +
                "?address :inCountry ?country . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(4, count);
    }

    @Test
    public void testStocktraders() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?fn ?ln ?securitynumber \n" +
                "WHERE {\n" +
                "?v a :StockTrader; \n" +
                " :firstName ?fn ; \n" +
                " :lastName ?ln ; \n" +
                " :ssn ?securitynumber . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testBrokersWorkForThemseleves() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?fn ?ln ?securitynum \n" +
                "WHERE {\n" +
                "?v a :StockBroker; \n" +
                " :tradesOnBehalfOf ?v ; \n" +
                " :firstName ?fname ; \n" +
                " :lastName ?ln ; \n" +
                " :ssn ?securitynum . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testBrokersWorkForPhysical() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?fname ?ln ?securitynum \n" +
                "WHERE {\n" +
                "?v a :StockExchangeMember; \n" +
                "   :tradesOnBehalfOf ?client1 . \n" +
                "?client1 a :PhysicalPerson . \n" +
                "?v :firstName ?fname ; \n" +
                "   :lastName ?ln ; \n" +
                "   :ssn ?securitynum . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testBrokersWorkForLegal() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?fname ?ln ?securitynum \n" +
                "WHERE {\n" +
                "?v a :StockExchangeMember; \n" +
                "   :tradesOnBehalfOf ?client1 . \n" +
                "?client1 a :LegalPerson . \n" +
                "?v :firstName ?fname ; \n" +
                "   :lastName ?ln ; \n" +
                "   :ssn ?securitynum . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testBrokersWorkForLegalPhysical() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?fname ?ln ?securitynum \n" +
                "WHERE {\n" +
                "?v a :StockExchangeMember; \n" +
                "   :tradesOnBehalfOf ?client1 . \n" +
                "?client1 a :PhysicalPerson . \n" +
                "?v :tradesOnBehalfOf ?client2 . \n" +
                "?client2 a :LegalPerson . \n" +
                "?v :firstName ?fname ; \n" +
                "   :lastName ?ln ; \n" +
                "   :ssn ?securitynum . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testTransactonsFinantialInstrument() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v \n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :involvesInstrument ?inst . \n" +
                "?inst a :FinantialInstrument . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(4, count);
    }

    @Test
    public void testTransactionStockType() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?inst ?type \n" +
                "WHERE {\n" +
                "?v a :Transaction; \n" +
                "   :involvesInstrument ?inst . \n" +
                "?inst a :Stock . \n" +
                "?inst :typeOfShares ?type . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(4, count);
    }

    @Test
    public void testTransactionOfferStock() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                "SELECT DISTINCT ?v ?inst ?type \n" +
                "WHERE {\n" +
                "?v a :Transaction . \n" +
                "?v a :Offer . \n" +
                "?v :involvesInstrument ?inst . \n" +
                "?inst a :Stock . \n" +
                "?inst :typeOfShares ?type . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(4, count);
    }
}
