package federationOptimization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.impl.QuestQueryProcessor;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.impl.SQLStandardQuotedIDFactory;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.impl.NativeNodeImpl;
import it.unibz.inf.ontop.iq.optimizer.FederationOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.query.KGQueryFactory;
import org.junit.Before;
import org.junit.Test;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import org.junit.experimental.categories.Category;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@Category(ObdfTest.class)
public class FederationOptimizerTest {

    private static final String owlFile = "src/test/resources/federation/ontology.owl";
    private static final String obdaFile = "src/test/resources/federation/mappings.fed.obda";
    private static final String propertyFile = "src/test/resources/federation/system-denodo-het.properties";
//    private static final String metadataFile = null;
    private static final String metadataFile = "src/test/resources/federation/system-denodo-het.metadata.json";
    private static final String constraintFile = "src/test/resources/federation/constraints.fed.txt";

    private static final String hintFile = "src/test/resources/federation/hints.denodo-optmatv.txt";
    private static final String effLabelFile = "src/test/resources/federation/source_efficiency_labels.het.txt";
    private static final boolean optimizationEnabled = true;
    private static final String sourceFile = "src/test/resources/federation/source_relations.txt";


    public OntopSQLOWLAPIConfiguration configuration;
    public static IntermediateQueryFactory IQ_FACTORY;
    public static AtomFactory ATOM_FACTORY;
    public static TermFactory TERM_FACTORY;
    public static CoreSingletons CORE_SINGLETONS;
    public static QuotedIDFactory idFactory;
    public static DBTypeFactory dbTypeFactory;
    public static FederationOptimizer federationOptimizer;

    @Before
    public void setUp() {
        try {
            OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration
                    .defaultBuilder()
                    .ontologyFile(owlFile)
                    //.lensesFile(lenseFile)
                    .basicImplicitConstraintFile(constraintFile)
                    .nativeOntopMappingFile(obdaFile)
                    .propertyFile(propertyFile)
                    .enableTestMode();
            if (metadataFile != null) {
                builder = builder.dbMetadataFile(metadataFile);
            }
            this.configuration = builder.build();

            Injector injector = configuration.getInjector();
            IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
            ATOM_FACTORY = injector.getInstance(AtomFactory.class);
            TERM_FACTORY = injector.getInstance(TermFactory.class);
            CORE_SINGLETONS = injector.getInstance(CoreSingletons.class);
            dbTypeFactory = CORE_SINGLETONS.getTypeFactory().getDBTypeFactory();
            idFactory = new SQLStandardQuotedIDFactory();

            federationOptimizer = new FederationOptimizerImpl(IQ_FACTORY, ATOM_FACTORY, TERM_FACTORY, CORE_SINGLETONS, optimizationEnabled, sourceFile, effLabelFile, hintFile);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @Test
    public void testFederationOptimizer() throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException, OBDASpecificationException, OntopReformulationException, IOException {
//        in this test case global environment variables are passed as arguments to the constructor
//        to run the test cases in obdalin mode, the following environment variables should be set:
//        ONTOP_OBDF_OPTIMIZATION_ENABLED=true;
//        ONTOP_OBDF_SOURCE_FILE=src/test/resources/federation/source_relations.txt;
//        ONTOP_OBDF_EFF_LABEL_FILE=src/test/resources/federation/source_efficiency_labels.het.txt;
//        ONTOP_OBDF_HINT_FILE=src/test/resources/federation/hints.denodo-optmatv.txt;

        ArrayList<String> queryFiles = new ArrayList<String>();
        queryFiles.add("src/test/resources/federation/bsbm-queries/01.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/02.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/03.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/04.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/05.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/06.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/07.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/08.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/09.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/10.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/11.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/12.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/13.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/14.rq");
        queryFiles.add("src/test/resources/federation/bsbm-queries/15.rq");

        for (String queryFile : queryFiles) {
            System.out.println("Query file: " + queryFile);
            testFederationOptimizer(queryFile);
        }
    }

    public void testFederationOptimizer(String queryFile) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException, OBDASpecificationException, OntopReformulationException, IOException {

        String inputIQFile = queryFile.replace("bsbm-queries/", "bsbm-queries/optimized-queries/").replace(".rq","-denodo-het-optmatv--no-opt.iq") ;
        String outputIQFile = queryFile.replace("bsbm-queries/", "bsbm-queries/optimized-queries/").replace(".rq","-denodo-het-optmatv--fed-opt.iq") ;
        String executableIQFile = queryFile.replace("bsbm-queries/", "bsbm-queries/optimized-queries/").replace(".rq","-denodo-het-optmatv--exec.iq") ;
        String outputSQLFile = queryFile.replace("bsbm-queries/", "bsbm-queries/optimized-queries/").replace(".rq","-denodo-het-optmatv.sql") ;

        String sparqlQuery = Files.readString(Path.of(queryFile));
        System.out.println("SPARQL query:\n" + sparqlQuery + "\n");

        QuestQueryProcessor.returnPlannedQuery = true;
        KGQueryFactory kgQueryFactory = configuration.getKGQueryFactory();
        KGQuery<?> query = kgQueryFactory.createSPARQLQuery(sparqlQuery);
        QueryReformulator reformulator = configuration.loadQueryReformulator();
        QueryLogger queryLogger = reformulator.getQueryLoggerFactory().create(ImmutableMultimap.of());
        QueryContext emptyQueryContext = reformulator.getQueryContextFactory().create(ImmutableMap.of());
        IQ iq = reformulator.reformulateIntoNativeQuery(query, emptyQueryContext, queryLogger);
        QuestQueryProcessor.returnPlannedQuery = false;

        // check if parsing of the query by Ontop is correct
        System.out.println("Parsed query converted into IQ:\n" + iq + "\n");
        String parsedQuery = Files.readString(Path.of(inputIQFile));
        assertEquals(parsedQuery, iq.toString());

        // Check if optimization of the query is correct
        IQ iqopt = federationOptimizer.optimize(iq);
        // Check if multiple application of normalizeForOptimization and optimize lead to a better query
//        IQ iqoptnew = iqopt.normalizeForOptimization();
//        int i=0;
//        while(!iqopt.toString().equals(iqoptnew.toString())) {
//            iqopt = federationOptimizer.optimize(iqoptnew);
//            iqoptnew = iqopt.normalizeForOptimization();
//            i++;
//            System.out.println("HALLO"+i);
//        }

        System.out.println("Optimized IQ:\n" + iqopt + "\n");
        String optimizedQuery = Files.readString(Path.of(outputIQFile));
//        assertEquals(optimizedQuery, iqopt.toString());

        // Check if the executable query is correct
        // The executable query is the query that contains the CONSTRUCT instructions for the VKG and the SQL query for the sources
        IQ iqexec = reformulator.generateExecutableQuery(iqopt);
        System.out.println("Executable IQ:\n" + iqexec + "\n");
        String executableQuery = Files.readString(Path.of(executableIQFile));
        assertEquals(executableQuery, iqexec.toString());

        // Check if the SQL query is correct
        String querysql = ((NativeNodeImpl) iqexec.getTree().getChildren().get(0)).getNativeQueryString();
        System.out.println("Final SQL query:\n" + querysql);
        String optimizedQuerySQL = Files.readString(Path.of(outputSQLFile));
        assertEquals(optimizedQuerySQL, querysql);
    }

    @Test
    public void testExtractSQLFromLogFile() throws IOException {
        // Assuming the log file path is correct and accessible
        Path path = Path.of("src/test/resources/federation/mixer_200_denodo-het_optmatv.log");

        // Reading the file line by line
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                if (line.contains("reformulatedQuery")) {
                    extractSQLFromLog(line);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String extractSQLFromLog(String inputString) {
        if (!inputString.contains("reformulatedQuery") || !inputString.contains("sparqlQuery") || !inputString.contains("payload")) {
            return null;
        }

        // Initialize ObjectMapper for parsing JSON
        ObjectMapper objectMapper = new ObjectMapper();

        // Find the start of the JSON data
        int jsonStartPos = inputString.indexOf('{');
        String jsonString = inputString.substring(jsonStartPos);

        try {
            // Parse the JSON string into a JsonNode
            JsonNode rootNode = objectMapper.readTree(jsonString);

            // Navigate through the JSON to extract the "reformulatedQuery"
            JsonNode reformulatedQueryNode = rootNode.path("payload").path("reformulatedQuery");
            String reformulatedQuery = reformulatedQueryNode.asText();

            int bsbmNumber = extractNumberFromSparqlQuery(rootNode.path("payload").path("sparqlQuery").asText());

            // Print the extracted SQL query for demonstration
            System.out.println("BSBM Query " + bsbmNumber + ":\nReformulated SQL Query:");
            System.out.println(reformulatedQuery);

            Files.writeString(Path.of("src/test/resources/federation/bsbm-queries/obdalin/"+ String.format("%02d", bsbmNumber) + "-denodo-het_optmatv--exec.iq"), reformulatedQuery);

            return reformulatedQuery;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int extractNumberFromSparqlQuery(String inputString) {
        // Regular expression to find "BSBM-" followed by any number
        Pattern pattern = Pattern.compile("BSBM-(\\d+):");
        Matcher matcher = pattern.matcher(inputString);

        if (matcher.find()) {
            // Return the first group (the number following "BSBM-")
            return Integer.parseInt(matcher.group(1));
        }

        // Return a default value or throw an exception if not found
        return -1; // Indicates not found or you can handle it differently
    }
}