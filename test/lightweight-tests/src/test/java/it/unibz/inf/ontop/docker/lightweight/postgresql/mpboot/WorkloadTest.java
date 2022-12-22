package it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.MPBootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.JoinPairs;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.sqlparser.WorkloadParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.docker.lightweight.postgresql.mpboot.utils.MPBootTestsHelper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@PostgreSQLLightweightTest
public class WorkloadTest {

    // Reference and to-be--compared files
    private static final String referenceOBDA = "src/test/resources/mpboot/spider_flights/reference-spider_flights.obda";
    private static final String referenceOWL = "src/test/resources/mpboot/spider_flights/reference-spider_flights.owl";

    // Local Queries
    private static final String testLocal = "src/test/resources/mpboot/spider_flights/spider_flights_queries_clean.json";

    // DB-connection
    private static final String owlPath = "src/test/resources/mpboot/spider_flights/spider_flight_2.owl";
    private static final String obdaPath = "src/test/resources/mpboot/spider_flights/spider_flight_2.obda";
    private static final String propertyPath = "/mpboot/spider_flights/spider_flight_2.properties";

    // Bootstrapping-info
    private static final String BASE_IRI = "http://semanticweb.org/flights/";
    private static final String bootOwlPath = "src/test/resources/mpboot/spider_flights/boot-spider_flights.owl";
    private static final String bootOBDAPath = "src/test/resources/mpboot/spider_flights/boot-spider_flights.obda";

    private final static String sql1 = "SELECT count(*) FROM FLIGHTS AS T1 JOIN AIRPORTS AS T2 ON T1.DestAirport  =  T2.AirportCode JOIN AIRPORTS AS T3 ON T1.SourceAirport  =  T3.AirportCode WHERE T2.City  = \"Teramo\"";
    private final static String sql2 = "SELECT count(*) FROM \"FLIGHTS\" AS T1 JOIN AIRPORTS AS T2 ON T1.DestAirport  =  T2.AirportCode JOIN AIRPORTS AS T3 ON T1.SourceAirport  =  T3.AirportCode WHERE T2.City  = \"Teramo\""; // Quoted
    private final static String sql3 = "SELECT count(*) FROM \"FLIGHTS\" AS T1 JOIN AIRPORTS AS T2 ON T1.DestAirport  =  T2.AirportCode WHERE T2.City  = \"Teramo\""; // QUOTED (in the joinPairs, I get "FLIGHTS"); SHOULD OVERLAP WITH existing FKEY
    private final static String sql4 = "SELECT count(*) FROM FLIGHTS T1, AIRPORTS T2 WHERE T1.DestAirport = T2.AirportCode AND T2.AirportCode = T1.SourceAirport AND T2.City  = \"Teramo\""; // Query with WHERE, plus a FILTER THAT is not a join-condition

    @Test
    public void testFlightsBootstrapping(){
        WorkloadParser parser = new WorkloadParser();

        try {
            // List<String>workload = getQueriesForDB("flight_2");
            List<String>workload = getWorkloadQueries(testLocal);
            JoinPairs pairs = new JoinPairs();
            for( String query : workload ){
                pairs.unite(parser.parseQuery(query)); // Side effect on empty
            }

            OntopSQLOWLAPIConfiguration initialConfiguration = configure(propertyPath, owlPath, obdaPath);
            BootConf bootConf = new BootConf.Builder()
                    .joinPairs(pairs)
                    .enableSH(false)
                    .build();

            Bootstrapper.BootstrappingResults results = bootstrapMapping(initialConfiguration, bootConf, BASE_IRI, Method.MPBOOT);

            SQLPPMapping bootstrappedMappings = results.getPPMapping();
            OWLOntology boootstrappedOnto = results.getOntology();

            // Serialize
            serializeMappingsAndOnto(bootstrappedMappings, boootstrappedOnto, bootOwlPath, bootOBDAPath);
        } catch (IOException | JSQLParserException | OWLOntologyStorageException | OWLOntologyCreationException |
                 MappingException | MappingBootstrappingException | InvalidQueryException e) {
            e.printStackTrace();
        }

        File refOBDAFile = new File(referenceOBDA);
        File refOWLFile = new File(referenceOWL);

        File bootOBDAFile = new File(bootOBDAPath);
        File bootOWLFile = new File(bootOwlPath);
        try {
            boolean isOBDAEqual = FileUtils.contentEquals(refOBDAFile, bootOBDAFile);
            boolean isOWLEqual =  FileUtils.contentEquals(refOWLFile, bootOWLFile);
            assertTrue(isOBDAEqual);
            assertTrue(isOWLEqual);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFlightsJoinPairsUnite(){
        WorkloadParser parser = new WorkloadParser();
        try {
            // List<String> workload = getQueriesForDB("flight_2");
            List<String> workload = getWorkloadQueries(testLocal);
            JoinPairs pairs = new JoinPairs();
            for( String query : workload ){
                pairs.unite(parser.parseQuery(query)); // Side effect on empty
            }
            assertEquals("[[AIRLINES.uid]->[FLIGHTS.\"Airline\"], [AIRPORTS.\"AirportCode\"]->[FLIGHTS.\"SourceAirport\"], [Airports.\"AirportCode\"]->[Flights.\"DestAirport\"], [FLIGHTS.\"DestAirport\", FLIGHTS.\"SourceAirport\"]->[AIRPORTS.\"AirportCode\", AIRPORTS.\"AirportCode\"], [AIRPORTS.\"AirportCode\"]->[FLIGHTS.\"DestAirport\"]]",
                    pairs.toString());
        } catch (IOException | JSQLParserException | InvalidQueryException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Test
    public void testParser(){
        WorkloadParser parser = new WorkloadParser();
        try {
            JoinPairs pairs1 = parser.parseQuery(sql1);
            JoinPairs pairs2 = parser.parseQuery(sql2);
            JoinPairs pairs3 = parser.parseQuery(sql3);
            JoinPairs pairs4 = parser.parseQuery(sql4);

            assertEquals("[[FLIGHTS.DestAirport, FLIGHTS.SourceAirport]->[AIRPORTS.AirportCode, AIRPORTS.AirportCode]]", pairs1.toString());
            assertEquals("[[\"FLIGHTS\".DestAirport, \"FLIGHTS\".SourceAirport]->[AIRPORTS.AirportCode, AIRPORTS.AirportCode]]", pairs2.toString());
            assertEquals("[[\"FLIGHTS\".DestAirport]->[AIRPORTS.AirportCode]]", pairs3.toString());
            assertEquals("[[FLIGHTS.DestAirport, FLIGHTS.SourceAirport]->[AIRPORTS.AirportCode, AIRPORTS.AirportCode]]", pairs4.toString());

            // testUnite
            pairs1.unite(pairs2);
            pairs1.unite(pairs3);
            pairs1.unite(pairs4);

            assertEquals("[[\"FLIGHTS\".DestAirport, \"FLIGHTS\".SourceAirport]->[AIRPORTS.AirportCode, AIRPORTS.AirportCode], [FLIGHTS.DestAirport, FLIGHTS.SourceAirport]->[AIRPORTS.AirportCode, AIRPORTS.AirportCode], [\"FLIGHTS\".DestAirport]->[AIRPORTS.AirportCode]]", pairs1.toString());
        } catch (JSQLParserException e) {
            e.printStackTrace();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }
}
