package federationOptimization;

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
import java.util.*;

import static org.junit.Assert.assertEquals;

@Category(ObdfTest.class)
public class FederationOptimizerTest {

    private static final String owlFile = "src/test/resources/federation/ontology.owl";
    private static final String obdaFile = "src/test/resources/federation/mappings.fed.obda";
    private static final String propertyFile = "src/test/resources/federation/system-denodo-het.properties";
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

        String sparqlQuery = readFile(queryFile);

        System.out.println("SPARQL query:\n" + sparqlQuery + "\n");

        QuestQueryProcessor.returnPlannedQuery = true;
        KGQueryFactory kgQueryFactory = configuration.getKGQueryFactory();
        KGQuery<?> query = kgQueryFactory.createSPARQLQuery(sparqlQuery);
        QueryReformulator reformulator = configuration.loadQueryReformulator();
        QueryLogger queryLogger = reformulator.getQueryLoggerFactory().create(ImmutableMultimap.of());
        QueryContext emptyQueryContext = reformulator.getQueryContextFactory().create(ImmutableMap.of());
        IQ iq = reformulator.reformulateIntoNativeQuery(query, emptyQueryContext, queryLogger);
        QuestQueryProcessor.returnPlannedQuery = false;

        System.out.println("Parsed query converted into IQ:\n" + iq + "\n");

        String parsedQuery = readFile(queryFile.replace("bsbm-queries", "bsbm-queries/optimized-queries").replace(".rq", "-no-federation-optimization.iq"));
        assertEquals(parsedQuery, iq.toString());

        IQ iqopt = federationOptimizer.optimize(iq);

        System.out.println("Optimized IQ:\n" + iqopt + "\n");

        String optimizedQuery = readFile(queryFile.replace("bsbm-queries", "bsbm-queries/optimized-queries").replace(".rq", "-optmatv.iq"));
        assertEquals(optimizedQuery, iqopt.toString());

        IQ executableQuery = reformulator.generateExecutableQuery(iqopt);
        System.out.println("Final SQL query:\n" +((NativeNodeImpl)executableQuery.getTree().getChildren().get(0)).getNativeQueryString());

        String optimizedQuerySQL = readFile(queryFile.replace("bsbm-queries", "bsbm-queries/optimized-queries").replace(".rq", "-denodo-optmatv.sql"));
        assertEquals(optimizedQuerySQL, executableQuery.toString());
    }

    // Custom method to read text from a file
    public static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(filePath);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    // Custom method to write text to a file
    public static void writeFile(String filePath, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             OutputStreamWriter osw = new OutputStreamWriter(fos);
             BufferedWriter writer = new BufferedWriter(osw)) {

            writer.write(content);
        }
    }

}