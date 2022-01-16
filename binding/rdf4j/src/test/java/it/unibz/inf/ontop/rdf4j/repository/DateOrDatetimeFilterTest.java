package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class DateOrDatetimeFilterTest extends AbstractRDF4JTest {

    private static final String CREATE_DB_FILE = "/date-or-datetime/date-or-datetime.sql";
    private static final String OBDA_FILE = "/date-or-datetime/date-or-datetime.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testYearDate() {
        String query = "PREFIX fhir: <http://hl7.org/fhir/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX ofn:<http://www.ontotext.com/sparql/functions/>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?e a fhir:Encounter . \n" +
                "  ?e fhir:Encounter.period  [ \n" +
                "  fhir:Period.startdate [ fhir:value ?start ]  ;\n" +
                "  fhir:Period.enddate   [ fhir:value ?end ] ] .\n" +
                "BIND (year(?start) as ?v)\n" +
                "FILTER (?v >= \"1950\"^^xsd:integer)\n" +
                "}";

        runQueryAndCompare(query, ImmutableSet.of("1955"));
    }

    @Test
    public void testYearDatetime() {
        String query = "PREFIX fhir: <http://hl7.org/fhir/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX ofn:<http://www.ontotext.com/sparql/functions/>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?e a fhir:Encounter . \n" +
                "  ?e fhir:Encounter.period  [ \n" +
                "  fhir:Period.start [ fhir:value ?start ]  ;\n" +
                "  fhir:Period.end   [ fhir:value ?end ] ] .\n" +
                "BIND (year(?start) as ?v)\n" +
                "FILTER (?v >= \"1950\"^^xsd:integer)\n" +
                "}";

        runQueryAndCompare(query, ImmutableSet.of("1955"));
    }

    @Test
    public void testOfnDaysBetweenDate() {
        String query = "PREFIX fhir: <http://hl7.org/fhir/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX ofn:<http://www.ontotext.com/sparql/functions/>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?e a fhir:Encounter . \n" +
                "  ?e fhir:Encounter.period  [ \n" +
                "  fhir:Period.startdate [ fhir:value ?start ]  ;\n" +
                "  fhir:Period.enddate   [ fhir:value ?end ] ] .\n" +
                "BIND (ofn:daysBetween(?start,?end) as ?v)\n" +
                "FILTER (?v >= \"12\"^^xsd:integer)\n" +
                "}";

        runQueryAndCompare(query, ImmutableSet.of("14"));
    }

    @Test
    public void testOfnDaysBetweenDatetime() {
        String query = "PREFIX fhir: <http://hl7.org/fhir/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX ofn:<http://www.ontotext.com/sparql/functions/>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?e a fhir:Encounter . \n" +
                "  ?e fhir:Encounter.period  [ \n" +
                "  fhir:Period.start [ fhir:value ?start ]  ;\n" +
                "  fhir:Period.end   [ fhir:value ?end ] ] .\n" +
                "BIND (ofn:daysBetween(?start,?end) as ?v)\n" +
                "FILTER (?v >= \"12\"^^xsd:integer)\n" +
                "}";

        runQueryAndCompare(query, ImmutableSet.of("14"));
    }
}
