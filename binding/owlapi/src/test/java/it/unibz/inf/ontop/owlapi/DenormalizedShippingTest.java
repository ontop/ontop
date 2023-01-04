package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;

public class DenormalizedShippingTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void before() throws SQLException, IOException {
        initOBDA("/shipping/create-db.sql", "/shipping/shipping.obda");
    }

    @AfterClass
    public static void after() throws OWLException, SQLException {
        release();
    }

    @Test
    public void testShipmentCountries1() throws Exception {
        String sparqlQuery = "PREFIX : <http://example.org/shipping/voc#>\n" +
                "SELECT * {\n" +
                "  ?s a :Shipment ; :from ?fromCountry ; :to ?toCountry . \n" +
                "  ?fromCountry a :Country . \n" +
                "  ?toCountry a :Country . \n" +
                "} ORDER BY ?s";

        String loweredSQL = checkReturnedValuesAndReturnSql(sparqlQuery, "s", ImmutableList.of(
                "<http://example.com/shipment/1>",
                "<http://example.com/shipment/2>"))
                .toLowerCase();
        assertFalse(loweredSQL.contains("union"));
        assertFalse(loweredSQL.contains("distinct"));
    }

    @Test
    public void testValues() throws Exception {
        String sparqlQuery = "PREFIX : <http://example.org/shipping/voc#>\n" +
                "SELECT * {\n" +
                "  VALUES ?year { 2019 2019 }\n" +
                "}";

        checkReturnedValues(sparqlQuery, "year", ImmutableList.of("\"2019\"^^xsd:integer", "\"2019\"^^xsd:integer"));
    }

    @Test
    public void testShipmentCountries2() throws Exception {
        String sparqlQuery = "PREFIX : <http://example.org/shipping/voc#>\n" +
                "SELECT * {\n" +
                "  ?s a :Shipment ; :from ?fromCountry ; :to ?toCountry . \n" +
                "  ?fromCountry a :Country . \n" +
                "  ?toCountry a :Country . \n" +
                "  VALUES ?year { 2019 2020 }\n" +
                "} ORDER BY ?s";

        String loweredSQL = checkReturnedValuesAndReturnSql(sparqlQuery, "s", ImmutableList.of(
                "<http://example.com/shipment/1>", "<http://example.com/shipment/1>",
                "<http://example.com/shipment/2>", "<http://example.com/shipment/2>"))
                .toLowerCase();
        assertFalse(loweredSQL.contains("distinct"));
    }

    @Test
    public void testShipmentCountries3() throws Exception {
        String sparqlQuery = "PREFIX : <http://example.org/shipping/voc#>\n" +
                "SELECT * {\n" +
                "  ?s a :Shipment ; :from ?fromCountry ; :to ?toCountry . \n" +
                "  ?fromCountry a :Country ; :name ?fromCountryName . \n" +
                "  ?toCountry a :Country ; :name ?toCountryName . \n" +
                "} ORDER BY ?s";

        String loweredSQL = checkReturnedValuesAndReturnSql(sparqlQuery, "s", ImmutableList.of(
                "<http://example.com/shipment/1>",
                "<http://example.com/shipment/2>"))
                .toLowerCase();
        assertFalse(loweredSQL.contains("union"));
        assertFalse(loweredSQL.contains("distinct"));
    }

    @Test
    public void testShipmentCountries4() throws Exception {
        String sparqlQuery = "PREFIX : <http://example.org/shipping/voc#>\n" +
                "SELECT * {\n" +
                "  ?s a :Shipment ; :from ?fromCountry ; :to ?toCountry . \n" +
                "  ?fromCountry a :Country . \n" +
                "  ?toCountry a :EuropeanCountry . \n" +
                "} ORDER BY ?s";

        checkReturnedValuesAndReturnSql(sparqlQuery, "s", ImmutableList.of(
                "<http://example.com/shipment/1>"));
    }

    @Ignore("TODO: support lifting the common filter above the union")
    @Test
    public void testShipmentCountries5() throws Exception {
        String sparqlQuery = "PREFIX : <http://example.org/shipping/voc#>\n" +
                "SELECT * {\n" +
                "  ?s a :Shipment ; :from ?fromCountry ; :to ?toCountry . \n" +
                "  ?fromCountry a :Country . \n" +
                "  ?toCountry a :EuropeanCountry . \n" +
                "} ORDER BY ?s";

        String loweredSQL = checkReturnedValuesAndReturnSql(sparqlQuery, "s", ImmutableList.of(
                "<http://example.com/shipment/1>"));
        assertFalse(loweredSQL.contains("union"));
        assertFalse(loweredSQL.contains("distinct"));
    }
}
