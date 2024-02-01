package federationOptimization;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Injector;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.impl.QuestQueryProcessor;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.dbschema.impl.SQLStandardQuotedIDFactory;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.NativeNodeImpl;
import it.unibz.inf.ontop.iq.optimizer.FederationOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.FederationOptimizerImpl;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.query.KGQueryFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import org.junit.experimental.categories.Category;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Array;
import java.sql.SQLOutput;
import java.util.*;

@Category(ObdfTest.class)
public class FederationOptimizerTest {

    private static final String owlFile = "src/test/resources/federation/ontology.owl";
    private static final String obdaFile = "src/test/resources/federation/mappings.fed.obda";
    private static final String propertyFile = "src/test/resources/federation/system-denodo-het.properties";
    private static final String metadataFile = "src/test/resources/federation/system-het.metadata.json";
    private static final String effLabel = "src/test/resources/federation/source_efficiency_labels.het.txt";
    private static final String constraintFile = "src/test/resources/federation/constraints.fed.txt";
//    private static final String queryFile = "src/test/resources/federation/bsbm-queries/01.SPARQL";


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

            federationOptimizer = new FederationOptimizerImpl(IQ_FACTORY, ATOM_FACTORY, TERM_FACTORY, CORE_SINGLETONS);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testFederationOptimizer() throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException, OBDASpecificationException, OntopReformulationException {

        ArrayList<String> queryFiles = new ArrayList<String>();
//        queryFiles.add("src/test/resources/federation/bsbm-queries/01.SPARQL");
//        queryFiles.add("src/test/resources/federation/bsbm-queries/02.SPARQL");
//        queryFiles.add("src/test/resources/federation/bsbm-queries/03.SPARQL");
//        queryFiles.add("src/test/resources/federation/bsbm-queries/04.SPARQL");
//        queryFiles.add("src/test/resources/federation/bsbm-queries/05.SPARQL");
//        queryFiles.add("src/test/resources/federation/bsbm-queries/06.SPARQL");
        queryFiles.add("src/test/resources/federation/bsbm-queries/07.SPARQL");
//        queryFiles.add("src/test/resources/federation/bsbm-queries/08.SPARQL");
//        queryFiles.add("src/test/resources/federation/bsbm-queries/09.SPARQL");
//        queryFiles.add("src/test/resources/federation/bsbm-queries/10.SPARQL");
//        queryFiles.add("src/test/resources/federation/bsbm-queries/11.SPARQL");
//        queryFiles.add("src/test/resources/federation/bsbm-queries/12.SPARQL");

        for (String queryFile : queryFiles) {
            System.out.println("Query file: " + queryFile);
            testFederationOptimizer(queryFile);
        }
    }

    public void testFederationOptimizer(String queryFile) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException, OBDASpecificationException, OntopReformulationException {
        String sparqlQuery = "";
        {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(queryFile)));
                String line = null;
                while((line=br.readLine()) != null ){
                    sparqlQuery = sparqlQuery + line +" ";
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

        IQ iqopt = federationOptimizer.optimize(iq);

        System.out.println("Optimized IQ:\n" + iqopt + "\n");

        IQ executableQuery = reformulator.generateExecutableQuery(iqopt);
        System.out.println("Final SQL query:\n" +((NativeNodeImpl)executableQuery.getTree().getChildren().get(0)).getNativeQueryString());
    }

}