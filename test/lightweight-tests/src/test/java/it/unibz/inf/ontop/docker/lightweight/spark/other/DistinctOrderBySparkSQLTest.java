package it.unibz.inf.ontop.docker.lightweight.spark.other;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import it.unibz.inf.ontop.docker.lightweight.SparkSQLLightweightTest;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SparkSQLLightweightTest
public class DistinctOrderBySparkSQLTest extends AbstractDockerRDF4JTest {

    private static final String OWL_FILE = "/prof/prof.owl";
    private static final String PROPERTIES_FILE = "/prof/spark/prof-spark.properties";
    private static final String OBDA_FILE = "/prof/spark/prof-spark.obda";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testDistinctWithOrderBy1() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?v :firstName ?n .\n" +
                "} ORDER BY DESC(?v)";

        executeAndCompareValues(query, ImmutableSet.of("<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/8>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/7>",
                        "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/6>",
                        "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/5>",
                        "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/4>",
                        "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/3>",
                        "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/2>",
                        "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1>"));
    }

    @Test
    public void testDistinctWithOrderBy2() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?v :firstName ?n .\n" +
                "} ORDER BY DESC(?n)";

        QueryEvaluationException thrown = assertThrows(QueryEvaluationException.class, () -> runQuery(query));
        assertTrue(Optional.ofNullable(thrown.getCause())
                .flatMap(e -> Optional.ofNullable(e.getCause()))
                .filter(e -> e.getMessage() != null
                        && e.getMessage().contains("The dialect requires ORDER BY conditions to be projected but a DISTINCT prevents some of them"))
                .isPresent());
    }

    @Test
    public void testDistinctWithOrderBy3() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?s :firstName ?v .\n" +
                "} ORDER BY DESC(?s)";

        QueryEvaluationException thrown = assertThrows(QueryEvaluationException.class, () -> runQuery(query));
        assertTrue(Optional.ofNullable(thrown.getCause())
                .flatMap(e -> Optional.ofNullable(e.getCause()))
                .filter(e -> e.getMessage() != null
                        && e.getMessage().contains("The dialect requires ORDER BY conditions to be projected but a DISTINCT prevents some of them"))
                .isPresent());
    }

    @Test
    public void testDistinctWithOrderBy4() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?s :firstName ?v .\n" +
                "} ORDER BY DESC(?v)";

        executeAndCompareValues(query, ImmutableSet.of("\"Roger\"^^xsd:string", "\"Michael\"^^xsd:string",
                "\"Mary\"^^xsd:string", "\"John\"^^xsd:string", "\"Johann\"^^xsd:string", "\"Frank\"^^xsd:string",
                "\"Diego\"^^xsd:string", "\"Barbara\"^^xsd:string"));
    }

    @Test
    public void testNoDistinctWithOrderBy1() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?v :firstName ?n .\n" +
                "} ORDER BY DESC(?v)";

        executeAndCompareValues(query, ImmutableSet.of("<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/8>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/7>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/6>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/5>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/4>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/3>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/2>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1>"));
    }

    @Test
    public void testNoDistinctWithOrderBy2() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?v :firstName ?n .\n" +
                "} ORDER BY DESC(?n)";

        executeAndCompareValues(query, ImmutableSet.of("<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/8>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/7>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/6>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/5>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/4>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/3>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/2>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1>"));
    }

    @Test
    public void testNoDistinctWithOrderBy3() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?s :firstName ?v .\n" +
                "} ORDER BY DESC(?s)";

        executeAndCompareValues(query, ImmutableSet.of("\"Roger\"^^xsd:string", "\"Michael\"^^xsd:string",
                "\"Mary\"^^xsd:string", "\"John\"^^xsd:string", "\"Johann\"^^xsd:string", "\"Frank\"^^xsd:string",
                "\"Diego\"^^xsd:string", "\"Barbara\"^^xsd:string"));
    }

    @Test
    public void testNoDistinctWithOrderBy4() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?s :firstName ?v .\n" +
                "} ORDER BY DESC(?v)";

        executeAndCompareValues(query, ImmutableSet.of("\"Roger\"^^xsd:string", "\"Michael\"^^xsd:string",
                "\"Mary\"^^xsd:string", "\"John\"^^xsd:string", "\"Johann\"^^xsd:string", "\"Frank\"^^xsd:string",
                "\"Diego\"^^xsd:string", "\"Barbara\"^^xsd:string"));
    }
}
