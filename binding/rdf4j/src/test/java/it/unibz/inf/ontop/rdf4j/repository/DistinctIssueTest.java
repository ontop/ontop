package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class DistinctIssueTest extends AbstractRDF4JTest {
    private static final String CREATE_DB_FILE = "/sparql-distinct/sparql-distinct.sql";
    private static final String OBDA_FILE = "/sparql-distinct/sparql-distinct.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testDistinct() {
        int count = runQueryAndCount("PREFIX fhir: <http://hl7.org/fhir/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX sct: <http://snomed.info/id/>\n" +
                "SELECT DISTINCT ?patient ?code ?cond_date\n" +
                "WHERE {\n" +
                "?cond fhir:Condition.subject / fhir:link ?patient .\n" +
                "?cond fhir:Condition.code ?cond_code .\n" +
                "?cond fhir:Condition.onsetDateTime / fhir:value ?cond_date .\n" +
                "?cond_code fhir:CodeableConcept.coding [  fhir:Coding.code    [ fhir:value ?code ] ; ] .\n" +
                "}");

        assertEquals(1, count);
    }
}
