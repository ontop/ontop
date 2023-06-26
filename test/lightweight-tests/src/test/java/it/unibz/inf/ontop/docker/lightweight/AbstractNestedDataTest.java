package it.unibz.inf.ontop.docker.lightweight;

import com.google.common.collect.ImmutableMultiset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/***
 * Class to test if nested data structure functionality is supported correctly.
 */
public abstract class AbstractNestedDataTest extends AbstractDockerRDF4JTest {

    protected static final String OBDA_FILE = "/nested/nested.obda";
    protected static final String OWL_FILE = null;

    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testUniqueConstraintsPreserved() {
        String query = "PREFIX : <http://nested.example.org/>" +
                "\n" +
                "SELECT  ?v " +
                "WHERE {" +
                "?v :income ?i . \n" +
                "}";

        String ontopSQLtranslation = reformulate(query);
        Assertions.assertFalse(supportsPositionVariable() && ontopSQLtranslation.toUpperCase().contains("DISTINCT"), "UniqueConstraints not preserved.");
    }

    @Test
    public void testFlattenWithPosition() throws Exception {
        String query = "PREFIX : <http://nested.example.org/>" +
                "\n" +
                "SELECT  ?v " +
                "WHERE {" +
                "?p :index ?v . \n" +
                "}";

        executeAndCompareValues(query, getFlattenWithPositionExpectedValues());
    }

    protected ImmutableMultiset getFlattenWithPositionExpectedValues() {
        return ImmutableMultiset.of( "\"1\"^^xsd:integer", "\"1\"^^xsd:integer", "\"1\"^^xsd:integer",
                "\"2\"^^xsd:integer", "\"2\"^^xsd:integer", "\"2\"^^xsd:integer", "\"3\"^^xsd:integer");
    }

    @Test
    public void testFlattenTimestamp() throws Exception {
        String query = "PREFIX : <http://nested.example.org/>" +
                "\n" +
                "SELECT  ?v " +
                "WHERE {" +
                "?p  :invoiceDate ?v . " +
                "}";

        executeAndCompareValues(query, getFlattenTimestampExpectedValues());
    }

    protected ImmutableMultiset getFlattenTimestampExpectedValues() {
        return ImmutableMultiset.of( "\"2023-01-01T18:00:00\"^^xsd:dateTime", "\"2023-01-15T18:00:00\"^^xsd:dateTime", "\"2023-01-29T12:00:00\"^^xsd:dateTime",
                "\"2023-02-12T18:00:00\"^^xsd:dateTime", "\"2023-02-26T18:00:00\"^^xsd:dateTime",
                "\"2023-03-12T18:00:00\"^^xsd:dateTime", "\"2023-03-26T18:00:00\"^^xsd:dateTime");
    }

    @Test
    public void testFlattenInteger() throws Exception {
        String query = "PREFIX : <http://nested.example.org/>" +
                "\n" +
                "SELECT  ?p ?v " +
                "WHERE {" +
                "?p  :invoiceAmount ?v . " +
                "}";

        executeAndCompareValues(query, getFlattenIntegerExpectedValues());
    }

    protected ImmutableMultiset getFlattenIntegerExpectedValues() {
        return ImmutableMultiset.of( "\"10000\"^^xsd:integer", "\"13000\"^^xsd:integer", "\"18000\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"14000\"^^xsd:integer", "\"15000\"^^xsd:integer", "\"20000\"^^xsd:integer");
    }

    @Test
    public void testFlatten2DArray() throws Exception {
        String query = "PREFIX : <http://nested.example.org/>" +
                "\n" +
                "SELECT  ?v " +
                "WHERE {" +
                "?p :hasWorker ?w. " +
                "?w  :name ?v . " +
                "}";

        executeAndCompareValues(query, getFlatten2DArrayExpectedValues());
    }

    protected ImmutableMultiset getFlatten2DArrayExpectedValues() {
        return ImmutableMultiset.of( "\"Sam\"^^xsd:string", "\"Cynthia\"^^xsd:string", "\"Cynthia\"^^xsd:string", "\"Cynthia\"^^xsd:string", "\"Bob\"^^xsd:string",
                "\"Bob\"^^xsd:string", "\"Bob\"^^xsd:string", "\"Jim\"^^xsd:string", "\"Jim\"^^xsd:string", "\"Jim\"^^xsd:string", "\"Carl\"^^xsd:string");
    }

    @Test
    public void testFlattenWithEmptyElement() throws Exception {
        String query = "PREFIX : <http://nested.example.org/>" +
                "\n" +
                "SELECT  ?v " +
                "WHERE {" +
                "?p  :id ?id . \n" +
                "?p :hasWorker ?w . \n" +
                "?w :name ?v ." +
                "FILTER(?id = 2)" +
                "}";

        assertEquals(getFlattenWithEmptyElement(), runQueryAndCount(query));
    }

    protected int getFlattenWithEmptyElement() {
        return 2;
    }

    @Test
    public void testFlattenJson() throws Exception {
        String query = "PREFIX : <http://nested.example.org/>" +
                "\n" +
                "SELECT  ?v " +
                "WHERE {" +
                "?m :firstName ?v. " +
                "}";

        executeAndCompareValues(query, getFlattenJsonExpectedValues());
    }

    protected ImmutableMultiset getFlattenJsonExpectedValues() {
        return ImmutableMultiset.of( "\"Mary\"^^xsd:string", "\"Carlos\"^^xsd:string", "\"John\"^^xsd:string", "\"Helena\"^^xsd:string", "\"Robert\"^^xsd:string",
                "\"Joseph\"^^xsd:string", "\"Godfrey\"^^xsd:string");
    }

    @Test
    public void testFlattenJsonPossiblyNull() throws Exception {
        String query = "PREFIX : <http://nested.example.org/>" +
                "\n" +
                "SELECT  ?v " +
                "WHERE {" +
                "?m :age ?v. " +
                "}";

        executeAndCompareValues(query, getFlattenJsonPossiblyNullExpectedValues());
    }

    protected ImmutableMultiset getFlattenJsonPossiblyNullExpectedValues() {
        return ImmutableMultiset.of( "\"28\"^^xsd:integer", "\"45\"^^xsd:integer", "\"60\"^^xsd:integer",
                "\"48\"^^xsd:integer", "\"59\"^^xsd:integer");
    }

    @Test
    public void testFlattenWithAggregate() throws Exception {
        String query = "PREFIX : <http://nested.example.org/>" +
                "\n" +
                "SELECT  (CONCAT(?name, ': ', STR(AVG(?value))) as ?v) " +
                "WHERE {" +
                "?p :hasWorker ?w. " +
                "?w :name ?name. " +
                "?p :invoiceAmount ?value. " +
                "} GROUP BY ?name ";

        executeAndCompareValues(query, getFlattenWithAggregateExpectedValues());
    }

    protected ImmutableMultiset getFlattenWithAggregateExpectedValues() {
        return ImmutableMultiset.of("\"Carl: 15000.0000000000000000\"^^xsd:string", "\"Jim: 15666.666666666667\"^^xsd:string",
                "\"Cynthia: 13000.0000000000000000\"^^xsd:string", "\"Sam: 10000.0000000000000000\"^^xsd:string",
                "\"Bob: 17666.666666666667\"^^xsd:string");
    }

    @Test
    public void testSPO() {
        String query = "PREFIX : <http://nested.example.org/>\n" + "SELECT * WHERE { ?s ?p ?o. }";
        assertEquals(getSPOExpectedCount(), runQueryAndCount(query));
    }

    protected int getSPOExpectedCount() {
        return 89;
    }

    protected boolean supportsPositionVariable() { return true; }

}
