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
    public static void before() throws OWLOntologyCreationException, SQLException, IOException {
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

    @Ignore("We need to be more robust to VALUES")
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
}
