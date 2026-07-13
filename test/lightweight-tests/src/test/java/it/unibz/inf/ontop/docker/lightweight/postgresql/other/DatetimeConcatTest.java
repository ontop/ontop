package it.unibz.inf.ontop.docker.lightweight.postgresql.other;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@PostgreSQLLightweightTest
public class DatetimeConcatTest extends AbstractDockerRDF4JTest {

    private static final String OWL_FILE = "/books/books.owl";
    private static final String OBDA_FILE = "/books/postgresql/books-timestamp.obda";
    private static final String PROPERTIES_FILE = "/books/postgresql/books-timestamp-postgresql.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Test
    public void testTimestampConcatenatedInMapping() {
        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT ?v WHERE {\n" +
                "  ?b rdfs:label ?v .\n" +
                "} ORDER BY ?v";

        executeAndCompareValues(query, ImmutableSet.of(
                "\"published on 1970-11-05T07:50:00\"^^xsd:string",
                "\"published on 2011-12-08T11:30:00\"^^xsd:string",
                "\"published on 2014-06-05T16:47:52\"^^xsd:string",
                "\"published on 2015-09-21T09:23:06\"^^xsd:string"));
    }
}
