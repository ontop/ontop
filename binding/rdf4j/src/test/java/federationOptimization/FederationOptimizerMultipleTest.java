package federationOptimization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.impl.NativeNodeImpl;
import it.unibz.inf.ontop.iq.optimizer.FederationOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.FederationOptimizerImpl;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.query.KGQueryFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("obdf")
public class FederationOptimizerMultipleTest {

    public static IntermediateQueryFactory IQ_FACTORY;
    public static AtomFactory ATOM_FACTORY;
    public static TermFactory TERM_FACTORY;
    public static CoreSingletons CORE_SINGLETONS;
    public static QuotedIDFactory idFactory;
    public static DBTypeFactory dbTypeFactory;
    private FederationOptimizer federationOptimizer;
    private OntopSQLOWLAPIConfiguration configuration;
    private QueryReformulator reformulator;
    private KGQueryFactory kgQueryFactory;

    private String expectedOutput;
    private String actualOutput;
    private List<Executable> assertions;

    @BeforeEach
    public void setupTest() {
        assertions = new ArrayList<>();
        expectedOutput = "";
        actualOutput = "";
    }

    public void setUp(String owlFile, String obdaFile, String propertyFile, String metadataFile, String constraintFile, boolean optimizationEnabled, String sourceFile, String effLabelFile, String hintFile) {
        try {
            OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration
                    .defaultBuilder().ontologyFile(owlFile)
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

            // Creata a kgQueryFactory to parse SPARQL queries
            kgQueryFactory = configuration.getKGQueryFactory();
            // Create a reformulator for transforming SPARQL queries into IQs
            reformulator = configuration.loadQueryReformulator();
            federationOptimizer = new FederationOptimizerImpl(IQ_FACTORY, ATOM_FACTORY, TERM_FACTORY, CORE_SINGLETONS, optimizationEnabled, sourceFile, effLabelFile, hintFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setup(Map<String, Object> config) {
        setUp((String) config.get("owlFile"), (String) config.get("obdaFile"), (String) config.get("propertyFile"), (String) config.get("metadataFile"), (String) config.get("constraintFile"), (boolean) config.get("optimizationEnabled"), (String) config.get("sourceFile"), (String) config.get("effLabelFile"), (String) config.get("hintFile"));
    }

    @Test
    public void testFederationOptimizerWithJson() throws Exception {

        for (FederationEngine federationEngine : FederationEngine.values()) {
            // Skip specific Federation Engines
            // if(federationEngine == FederationEngine.DREMIO) continue;
            // if(federationEngine == FederationEngine.TEIID) continue;
            // if(federationEngine == FederationEngine.DENODO) continue;

            for (FederationSetting federationSetting : FederationSetting.values()) {
                // Skip specific Federation Settings
                // if(federationSetting == FederationSetting.HOM) continue;
                // if(federationSetting == FederationSetting.HET) continue;

                for (FederationOptimization federationOptimization : FederationOptimization.values()) {
                    // Skip specific Federation Optimizations
                    // if(federationOptimization == FederationOptimization.OPT) continue;
                    // if(federationOptimization == FederationOptimization.OPTMATV) continue;

                    // Build and log the header detail
                    logAndRecordOutput(buildDetails(federationEngine, federationSetting, federationOptimization));

                    // Setup configuration and process queries
                    Map<String, Object> config = createDefaultBSBMTestConfiguration(federationEngine, federationSetting, federationOptimization);
                    setup(config);
                    for (Map<String, String> queryInfo : ((List<Map<String, String>>) config.get("queries"))) {
                        // Conditional skipping of specific queries based on file name patterns
//                        if(queryInfo.get("queryFile").contains("01")) continue;
//                        if(queryInfo.get("queryFile").contains("02")) continue;
//                        if(queryInfo.get("queryFile").contains("03")) continue;
//                        if(queryInfo.get("queryFile").contains("04")) continue;
//                        if(queryInfo.get("queryFile").contains("05")) continue;
//                        if(queryInfo.get("queryFile").contains("06")) continue;
//                        if(queryInfo.get("queryFile").contains("07")) continue;
//                        if(queryInfo.get("queryFile").contains("08")) continue;
//                        if(queryInfo.get("queryFile").contains("09")) continue;
//                        if(queryInfo.get("queryFile").contains("10")) continue;
//                        if(queryInfo.get("queryFile").contains("11")) continue;
//                        if(queryInfo.get("queryFile").contains("12")) continue;
//                        if(queryInfo.get("queryFile").contains("13")) continue;
//                        if(queryInfo.get("queryFile").contains("14")) continue;
//                        if(queryInfo.get("queryFile").contains("15")) continue;
//                        testFederationOptimizer(queryInfo, true, federationEngine, federationSetting, federationOptimization);
                        testFederationOptimizer(queryInfo, false, federationEngine, federationSetting, federationOptimization);
                    }
                }
            }
        }
        assertEquals(expectedOutput, actualOutput);
//        assertAll("Federation Optimization Tests", assertions);
    }

    private String buildDetails(FederationEngine federationEngine, FederationSetting federationSetting, FederationOptimization federationOptimization) {
        return "\n****************************************************************************************\n" +
                "Federation Engine: " + federationEngine + "\n" +
                "Federation Setting: " + federationSetting + "\n" +
                "Federation Optimization: " + federationOptimization + "\n" +
                "****************************************************************************************\n";
    }

    private void logAndRecordOutput(String output) {
        System.out.println(output);
        expectedOutput += output;
        actualOutput += output;
    }

    public void testFederationOptimizer(Map<String, String> queryInfo, boolean writeToFile, FederationEngine federationEngine, FederationSetting federationSetting, FederationOptimization federationOptimization) throws Exception {
        String queryFile = queryInfo.get("queryFile");
        String inputIQFile = queryInfo.get("inputIQFile");
        String outputIQFile = queryInfo.get("outputIQFile");
        String executableIQFile = queryInfo.get("executableIQFile");
        String outputSQLFile = queryInfo.get("outputSQLFile");

        String queryInfoString = "\n" + federationEngine + " " + federationSetting + " " + federationOptimization + " " + Paths.get(queryFile).getFileName() + "\n";

        // Load and process SPARQL query
        String sparqlQuery = Files.readString(Path.of(queryFile));
        logStep("SPARQL query:", sparqlQuery, queryInfoString, writeToFile, queryFile);

        // Processing IQ from SPARQL, optimization, and executable query generation
        IQ inputIQ = getIQFromSPARQL(sparqlQuery);
        logStep("Parsed query converted into IQ:", inputIQ.toString(), queryInfoString, writeToFile, inputIQFile);

        IQ optimizedIQ = federationOptimizer.optimize(inputIQ);
        logStep("Optimized IQ:", optimizedIQ.toString(), queryInfoString, writeToFile, outputIQFile);

        IQ executableIQ = reformulator.generateExecutableQuery(optimizedIQ);
        logStep("Executable IQ:", executableIQ.toString(), queryInfoString, writeToFile, executableIQFile);

        // Handling final SQL query
        String finalSQLQuery = ((NativeNodeImpl) executableIQ.getTree().getChildren().get(0)).getNativeQueryString(); // Implement this method as needed
        logStep("Final SQL query:", finalSQLQuery.toString(), queryInfoString, writeToFile, outputSQLFile);
    }

    private void logStep(String stepDescription, String data, String queryInfoString, boolean writeToFile, String filePath) throws IOException {
        System.out.println(queryInfoString + stepDescription + "\n" + data);
        if (writeToFile) {
            Files.writeString(Path.of(filePath), data);
        }
        compareWithFileContent(data, Files.readString(Path.of(filePath)), stepDescription, queryInfoString);
    }

    private void compareWithFileContent(String data, String fileContent, String description, String queryInfoString) {
        if (!fileContent.equals(data)) {
            expectedOutput += "\nAssertionError detected check below\n";
            actualOutput += "\nAssertionError detected check below\n";
        }
        expectedOutput += queryInfoString + description + fileContent + "\n";
        actualOutput += queryInfoString + description + data + "\n";

        assertions.add(() -> Assertions.assertEquals(fileContent, data, "\nAssertionError detected check below\n" + queryInfoString + description + fileContent));
    }

    public static void writeToFileCheckOverwrite(String filePath, String content) throws Exception {
        Path path = Path.of(filePath);

        if (Files.exists(path)) {
            String fileContent = Files.readString(path);
            if(!fileContent.equals(content)) {
                throw new Exception("File already exists and content is different");
            }
        } else {
            // Overwrite the file or create a new one if it doesn't exist
            Files.writeString(path, content);
        }
    }

    public void testFederationOptimizer(Map<String, String> queryInfo) throws Exception {
        testFederationOptimizer(queryInfo, false, null, null, null);
    }

    private IQ getIQFromSPARQL(String sparqlQuery) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException, OntopReformulationException {
        // Parse the SPARQL query into an IQ
        KGQuery<?> query = kgQueryFactory.createSPARQLQuery(sparqlQuery);
        QueryLogger queryLogger = reformulator.getQueryLoggerFactory().create(ImmutableMultimap.of());
        QueryContext emptyQueryContext = reformulator.getQueryContextFactory().create(ImmutableMap.of());
        QuestQueryProcessor.returnPlannedQuery = true;
        IQ iq = reformulator.reformulateIntoNativeQuery(query, emptyQueryContext, queryLogger);
        QuestQueryProcessor.returnPlannedQuery = false;
        return iq;
    }

    @Test
    public void testCreateDefaultBSBMTestConfiguration() throws Exception {
        for (FederationEngine federationEngine : FederationEngine.values()) {
            for (FederationSetting federationSetting : FederationSetting.values()) {
                for (FederationOptimization federationOptimization : FederationOptimization.values()) {
                    Map<String, Object> testConfig = createDefaultBSBMTestConfiguration(federationEngine, federationSetting, federationOptimization);

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                    String json = objectMapper.writeValueAsString(testConfig);
//                    In case of an update of the test configuration, the following line can be used to write the JSON to a file
                    writeToFileCheckOverwrite("src/test/resources/federation/config-files/test-config-" + federationEngine.name().toLowerCase() + "-" + federationSetting.name().toLowerCase() + "-" + federationOptimization.name().toLowerCase() + ".json", json);
                    System.out.println("Serialized JSON:\n" + json);

                    json = Files.readString(Path.of("src/test/resources/federation/config-files/test-config-" + federationEngine.name().toLowerCase() + "-" + federationSetting.name().toLowerCase() + "-" + federationOptimization.name().toLowerCase() + ".json"));
                    // Deserialize JSON back into a Map
                    Map<String, Object> deserializedMap = objectMapper.readValue(json, Map.class);
                    System.out.println("Deserialized Map:\n" + deserializedMap);

                    assertEquals(testConfig, deserializedMap);
                }
            }
        }
    }

    public static Map<String, Object> createDefaultTestConfiguration(FederationEngine federationEngine, FederationSetting federationSetting, FederationOptimization federationOptimization) {
        String federationEngineStr = federationEngine.name().toLowerCase();
        String federationSettingStr = federationSetting.name().toLowerCase();
        String federationOptimizationStr = federationOptimization.name().toLowerCase();

        Map<String, Object> testConfiguration = new HashMap<>();
        testConfiguration.put("name", federationEngineStr + "_" + federationSettingStr + "_" + federationOptimizationStr);
        testConfiguration.put("owlFile", "src/test/resources/federation/ontology.owl");
        // obdaFile is different for Teiid
        testConfiguration.put("obdaFile", "src/test/resources/federation/mappings.fed" + (federationEngine == FederationEngine.TEIID ? ".teiid" : "") + ".obda");
        testConfiguration.put("propertyFile", "src/test/resources/federation/system-" + federationEngineStr + "-" + federationSettingStr + ".properties");
        // metadata currently only supported for Denodo, else null
//        testConfiguration.put("metadataFile", (federationEngine == FederationEngine.DENODO ? "src/test/resources/federation/system-" + federationEngineStr + "-" + federationSettingStr + ".metadata.json" : null));
        testConfiguration.put("metadataFile", null);
        testConfiguration.put("constraintFile", "src/test/resources/federation/constraints.fed.txt");
        testConfiguration.put("hintFile", "src/test/resources/federation/hints." + federationEngineStr + "-" + federationOptimizationStr + ".txt");
        testConfiguration.put("effLabelFile", "src/test/resources/federation/source_efficiency_labels." + federationSettingStr + ".txt");
        testConfiguration.put("optimizationEnabled", true);
        testConfiguration.put("sourceFile", "src/test/resources/federation/source_relations." + federationSettingStr + ".txt");

        testConfiguration.put("queries", null);

        return testConfiguration;
    }

    public static Map<String, Object> createDefaultBSBMTestConfiguration(FederationEngine federationEngine, FederationSetting federationSetting, FederationOptimization federationOptimization, List<Integer> bsbm_queries) {

        Map<String, Object> testConfiguration = createDefaultTestConfiguration(federationEngine, federationSetting, federationOptimization);

        if (bsbm_queries != null) {
            // Create a list to store query information
            List<Map<String, String>> queries = new ArrayList<>();
            for (Integer queryNumber : bsbm_queries) {
                Map<String, String> queryInfo = new HashMap<>();
                String formattedI = String.format("%02d", queryNumber);
                queryInfo.put("queryFile", "src/test/resources/federation/bsbm-queries/" + formattedI + ".rq");
                queryInfo.put("inputIQFile", "src/test/resources/federation/bsbm-queries/optimized-queries/" + formattedI + "-" + federationEngine.name().toLowerCase() + "-" + federationSetting.name().toLowerCase() + "-" + federationOptimization.name().toLowerCase() + "--no-opt.iq");
                queryInfo.put("outputIQFile", "src/test/resources/federation/bsbm-queries/optimized-queries/" + formattedI + "-" + federationEngine.name().toLowerCase() + "-" + federationSetting.name().toLowerCase() + "-" + federationOptimization.name().toLowerCase() + "--fed-opt.iq");
                queryInfo.put("executableIQFile", "src/test/resources/federation/bsbm-queries/optimized-queries/" + formattedI + "-" + federationEngine.name().toLowerCase() + "-" + federationSetting.name().toLowerCase() + "-" + federationOptimization.name().toLowerCase() + "--exec.iq");
                queryInfo.put("outputSQLFile", "src/test/resources/federation/bsbm-queries/optimized-queries/" + formattedI + "-" + federationEngine.name().toLowerCase() + "-" + federationSetting.name().toLowerCase() + "-" + federationOptimization.name().toLowerCase() + ".sql");
                queries.add(queryInfo);
            }
            testConfiguration.put("queries", queries);
        } else {
            testConfiguration.put("queries", null);
        }
        return testConfiguration;
    }

    public static Map<String, Object> createDefaultBSBMTestConfiguration(FederationEngine federationEngine, FederationSetting federationSetting, FederationOptimization federationOptimization) {
        List<Integer> bsbm_queries = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            bsbm_queries.add(i);
        }

        return createDefaultBSBMTestConfiguration(federationEngine, federationSetting, federationOptimization, bsbm_queries);
    }

    enum FederationEngine {
        TEIID, DENODO, DREMIO
    }

    enum FederationSetting {
        HOM, HET
    }

    enum FederationOptimization {
        OPT, OPTMATV
    }
}
