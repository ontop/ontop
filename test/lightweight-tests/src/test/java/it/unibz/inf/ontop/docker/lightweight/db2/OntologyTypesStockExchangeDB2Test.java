package it.unibz.inf.ontop.docker.lightweight.db2;

import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import it.unibz.inf.ontop.docker.lightweight.DB2LightweightTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

@DB2LightweightTest
public class OntologyTypesStockExchangeDB2Test extends AbstractDockerRDF4JTest {
    private static final String OBDA_FILE = "/stockexchange/db2/stockexchange-db2.obda";
    private static final String OWL_FILE = "/stockexchange/stockexchange.owl";
    private static final String PROPERTIES_FILE = "/stockexchange/db2/stockexchange-db2.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    //we need xsd:string to work correctly
    @Test
    public void testQuotedLiteral() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT ?v ?street\n" +
                "\nWHERE {" +
                " ?v a :Address; " +
                "    :inStreet ?street; \n" +
                "    :inCity \"Bolzano\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        Assertions.assertEquals(2, count);
    }

    @Test
    public void testDatatypeString() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT ?v ?street\n" +
                "WHERE {\n" +
                " ?v a :Address; \n" +
                "    :inStreet ?street; \n" +
                "    :inCity \"Bolzano\"^^xsd:string .\n" +
                "}";
        int count = runQueryAndCount(query);
        Assertions.assertEquals(2, count);
    }

    //we need xsd:string to work correctly
    @Test
    public void testAddressesQuotedLiteral() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT *\n" +
                "WHERE {\n" +
                 "?v a :Address . \n" +
                 "?v :addressID $id. \n" +
                 "?v :inStreet \"Via Marconi\". \n" +
                 "?v :inCity \"Bolzano\". \n" +
                 "?v :inCountry $country. \n" +
                 "?v :inState $state. \n" +
                 "?v :hasNumber $number.\n" +
                "}";

        int count = runQueryAndCount(query);
        Assertions.assertEquals(1, count);
    }

    //in db2 there is no boolean type we refer to it in the database with a smallint 1 for true and a smallint 0 for false
    @Test
    public void testBooleanDatatype() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                " ?v a :Stock; \n" +
                "    :amountOfShares ?amount; \n" +
                "    :typeOfShares \"1\"^^xsd:integer . \n" +
                "}";

        int count = runQueryAndCount(query);
        Assertions.assertEquals(5, count);
    }

    @Test
    public void testBooleanInteger() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                " ?v a :Stock; \n" +
                "    :amountOfShares ?amount; \n" +
                "    :typeOfShares 1 . \n" +
                "}";

        int count = runQueryAndCount(query);
        Assertions.assertEquals(5, count);
    }

    //in db2 there is no boolean datatype, it is substituted with smallint
    @Test
    public void testBoolean() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                " ?v a :Stock; \n" +
                "    :amountOfShares ?amount; \n" +
                "    :typeOfShares TRUE . \n" +
                "}";

        int count = runQueryAndCount(query);
        Assertions.assertEquals(0, count);
    }

    //in db2 there is no boolean datatype, it is substituted with smallint
    @Test
    public void testBooleanTrueDatatype() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                " ?v a :Stock; \n" +
                "    :amountOfShares ?amount; \n" +
                "    :typeOfShares \"1\"^^xsd:boolean . \n" +
                "}";

        int count = runQueryAndCount(query);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void testFilterBoolean() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                " ?v a :Stock;" +
                "      :amountOfShares ?amount; \n" +
                "      :typeOfShares ?type . \n" +
                " FILTER ( ?type = 1 ). \n" +
                "}";

        int count = runQueryAndCount(query);
        Assertions.assertEquals(5, count);
    }

    @Test
    public void testNotFilterBoolean() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT ?v ?amount\n" +
                "WHERE {\n" +
                " ?v a :Stock; \n" +
                "    :amountOfShares ?amount; \n" +
                "    :typeOfShares ?type . \n" +
                " FILTER ( ?type != 1 ) .\n" +
                "}";

        int count = runQueryAndCount(query);
        Assertions.assertEquals(5, count);
    }

    //a quoted integer is treated as a literal
    @Test
    public void testQuotedInteger() {

        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT ?x\n" +
                "WHERE {\n" +
                " ?v a :Stock; \n" +
                "    :amountOfShares ?amount; \n" +
                "    :typeOfShares \"1\" .\n" +
                "}";

        int count = runQueryAndCount(query);
        Assertions.assertEquals(0, count);
    }


    @Test
    public void testDatetime() {

        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n " +
                "SELECT DISTINCT ?v " +
                "WHERE { " +
                " ?v a :Transaction; " +
                "      :transactionID ?id; \n" +
                "      :transactionDate ?d . \n" +
                " FILTER(?d = \"2008-04-02T00:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                "}";

        int count = runQueryAndCount(query);
        Assertions.assertEquals(1, count);
    }

    //a quoted datatype is treated as a literal
    @Test
    public void testQuotedDatatype() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                " ?v a :Transaction; \n" +
                "    :transactionID ?id; \n" +
                "    :transactionDate \"2008-04-02T00:00:00\" . \n" +
                "}";

        int count = runQueryAndCount(query);
        Assertions.assertEquals(0, count);
    }

    // The ontology contains three facts (Joe, Jane, Bane are Investors), the database contains three more.
    @Test
    public void testAbox() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                " ?v a :Investor . \n" +
                "}";
        int count = runQueryAndCount(query);
        Assertions.assertEquals(6, count);
    }

    @Disabled("DB2 instance only includes TIMESTAMP WITHOUT TIME ZONE. " +
            "Latest free version of DB does not support TIMESTAMP WITH TIME ZONE for all OS-s")
    @Test
    public void testDatetimeTimezone() {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n " +
                "SELECT DISTINCT ?v WHERE { \n" +
                "  ?v a :Transaction; \n" +
                "     :transactionID ?id; \n" +
                "     :transactionDate ?d . \n" +
                "  FILTER (?d = \"2008-04-02T00:00:00+06:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) \n" +
                "}";

        int count = runQueryAndCount(query);
        Assertions.assertEquals(1, count);
    }
}
