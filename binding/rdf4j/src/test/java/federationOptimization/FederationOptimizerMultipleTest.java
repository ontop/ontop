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
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FederationOptimizerMultipleTest {

    public OntopSQLOWLAPIConfiguration configuration;
    public static IntermediateQueryFactory IQ_FACTORY;
    public static AtomFactory ATOM_FACTORY;
    public static TermFactory TERM_FACTORY;
    public static CoreSingletons CORE_SINGLETONS;
    public static QuotedIDFactory idFactory;
    public static DBTypeFactory dbTypeFactory;
    public static FederationOptimizer federationOptimizer;

    public void setUp(String owlFile, String obdaFile, String propertyFile, String metadataFile, String constraintFile, boolean optimizationEnabled, String sourceFile, String effLabelFile, String hintFile) {
        try {
            OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder().ontologyFile(owlFile)
                    //.lensesFile(lenseFile)
                    .basicImplicitConstraintFile(constraintFile).nativeOntopMappingFile(obdaFile).propertyFile(propertyFile).enableTestMode();
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setup(Map<String, Object> config) {
        setUp((String) config.get("owlFile"), (String) config.get("obdaFile"), (String) config.get("propertyFile"), (String) config.get("metadataFile"), (String) config.get("constraintFile"), (boolean) config.get("optimizationEnabled"), (String) config.get("sourceFile"), (String) config.get("effLabelFile"), (String) config.get("hintFile"));
    }

    @Test
    public void testFederationOptimizerWithJson() throws OBDASpecificationException, OntopUnsupportedKGQueryException, OntopReformulationException, IOException, OntopInvalidKGQueryException {
        Map<String, Object> config = createDefaultTestConfiguration(FederationEngine.DENODO, FederationSetting.HET, FederationOptimization.OPTMATV);
        testFederationOptimizer(config);
    }

    public void testFederationOptimizer(Map<String, Object> config) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException, OBDASpecificationException, OntopReformulationException, IOException {

        setup(config);

        for (Map<String, String> queryInfo : ((List<Map<String, String>>) config.get("queries"))) {

            String queryFile = queryInfo.get("queryFile");
            String inputIQFile = queryInfo.get("inputIQFile");
            String outputIQFile = queryInfo.get("outputIQFile");
            String outputSQLFile = queryInfo.get("outputSQLFile");

            String sparqlQuery = this.readFile(queryFile);

            System.out.println("SPARQL query:\n" + sparqlQuery + "\n");

            KGQueryFactory kgQueryFactory = configuration.getKGQueryFactory();
            KGQuery<?> query = kgQueryFactory.createSPARQLQuery(sparqlQuery);
            QueryReformulator reformulator = configuration.loadQueryReformulator();
            QueryLogger queryLogger = reformulator.getQueryLoggerFactory().create(ImmutableMultimap.of());
            QueryContext emptyQueryContext = reformulator.getQueryContextFactory().create(ImmutableMap.of());
            QuestQueryProcessor.returnPlannedQuery = true;
            IQ iq = reformulator.reformulateIntoNativeQuery(query, emptyQueryContext, queryLogger);
            QuestQueryProcessor.returnPlannedQuery = false;

            System.out.println("Parsed query converted into IQ:\n" + iq + "\n");

            String parsedQuery = this.readFile(inputIQFile);
            assertEquals(parsedQuery, iq.toString());

            IQ iqopt = federationOptimizer.optimize(iq);

            System.out.println("Optimized IQ:\n" + iqopt + "\n");

            String optimizedQuery = this.readFile(outputIQFile);
            assertEquals(optimizedQuery, iqopt.toString());

            IQ executableQuery = reformulator.generateExecutableQuery(iqopt);
            System.out.println("Final SQL query:\n" + ((NativeNodeImpl) executableQuery.getTree().getChildren().get(0)).getNativeQueryString());

            String optimizedQuerySQL = this.readFile(outputSQLFile);
            assertEquals(optimizedQuerySQL, executableQuery.toString());
        }
    }

    // Custom method to read text from a file
    public String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(filePath); InputStreamReader isr = new InputStreamReader(fis); BufferedReader reader = new BufferedReader(isr)) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    // Custom method to write text to a file
    public void writeFile(String filePath, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath); OutputStreamWriter osw = new OutputStreamWriter(fos); BufferedWriter writer = new BufferedWriter(osw)) {
            writer.write(content);
        }
    }

    @Test
    public void testTestConfiguration() throws IOException {
        try {
            Map<String, Object> testConfig = createDefaultTestConfiguration(FederationEngine.DENODO, FederationSetting.HET, FederationOptimization.OPTMATV);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String json = objectMapper.writeValueAsString(testConfig);
            this.writeFile("src/test/resources/federation/config-files/test-config.json", json);
//            System.out.println("Serialized JSON:\n" + json);

            json = this.readFile("src/test/resources/federation/test-config.json");
            // Deserialize JSON back into a Map
            Map<String, Object> deserializedMap = objectMapper.readValue(json, Map.class);
            System.out.println("Deserialized Map:\n" + deserializedMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> createDefaultTestConfiguration(FederationEngine enum1, FederationSetting enum2, FederationOptimization enum3, List<Integer> bsbm_queries) {
        Map<String, Object> testConfiguration = new HashMap<>();
        testConfiguration.put("name", enum1.name().toLowerCase() + "_" + enum2.name().toLowerCase() + "_" + enum3.name().toLowerCase());
        testConfiguration.put("owlFile", "src/test/resources/federation/ontology.owl");
        testConfiguration.put("obdaFile", "src/test/resources/federation/mappings.fed.obda");
        testConfiguration.put("propertyFile", "src/test/resources/federation/system-" + enum1.name().toLowerCase() + "-" + enum2.name().toLowerCase() + ".properties");
        testConfiguration.put("metadataFile", "src/test/resources/federation/system-" + enum1.name().toLowerCase() + "-" + enum2.name().toLowerCase() + ".metadata.json");
        testConfiguration.put("constraintFile", "src/test/resources/federation/constraints.fed.txt");
        testConfiguration.put("hintFile", "src/test/resources/federation/hints." + enum1.name().toLowerCase() + "-" + enum3.name().toLowerCase() + ".txt");
        testConfiguration.put("effLabelFile", "src/test/resources/federation/source_efficiency_labels." + enum2.name().toLowerCase() + ".txt");
        testConfiguration.put("optimizationEnabled", true);
        testConfiguration.put("sourceFile", "src/test/resources/federation/source_relations.txt");

        // Create a list to store query information
        List<Map<String, String>> queries = new ArrayList<>();
        for (Integer queryNumber : bsbm_queries) {
            Map<String, String> queryInfo = new HashMap<>();
            String formattedI = String.format("%02d", queryNumber);
            queryInfo.put("queryFile", "src/test/resources/federation/bsbm-queries/" + formattedI + ".rq");
            queryInfo.put("inputIQFile", "src/test/resources/federation/bsbm-queries/optimized-queries/" + formattedI + "-no-federation-optimization.iq");
            queryInfo.put("outputIQFile", "src/test/resources/federation/bsbm-queries/optimized-queries/" + formattedI + "-optmatv.iq");
            queryInfo.put("outputSQLFile", "src/test/resources/federation/bsbm-queries/optimized-queries/" + formattedI + "-" + enum1.name().toLowerCase() + "-optmatv.sql");
            queries.add(queryInfo);
        }

        testConfiguration.put("queries", queries);

        return testConfiguration;
    }

    public static Map<String, Object> createDefaultTestConfiguration(FederationEngine enum1, FederationSetting enum2, FederationOptimization enum3) {
        List<Integer> bsbm_queries = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            bsbm_queries.add(i);
        }
        return createDefaultTestConfiguration(enum1, enum2, enum3, bsbm_queries);
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
