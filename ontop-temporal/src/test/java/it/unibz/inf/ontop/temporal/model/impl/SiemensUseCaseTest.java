package it.unibz.inf.ontop.temporal.model.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopEngineFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;
import it.unibz.inf.ontop.sql.RDBMetadata;
import it.unibz.inf.ontop.sql.RDBMetadataExtractionTools;
import it.unibz.inf.ontop.temporal.model.*;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class SiemensUseCaseTest extends TestCase {

    //private static final SQLMappingFactory MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
    //private final SpecificationFactory specificationFactory;
    private Connection connection;

    private RDBMetadata md;
    private PrefixManager pm;

    public SiemensUseCaseTest() {

//        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
//                .enableTestMode()
//                .build();
//
//        Injector injector = defaultConfiguration.getInjector();
//        specificationFactory = injector.getInstance(SpecificationFactory.class);
//
//        try {
//            connection = DriverManager.getConnection("jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp", "postgres", "postgres");
//            md = RDBMetadataExtractionTools.createMetadata(connection);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        // Prefix manager
//        Map<String, String> prefixes = new HashMap<>();
//        prefixes.put(":", "http://www.siemens.com/university#");
//        pm = specificationFactory.createPrefixManager(ImmutableMap.copyOf(prefixes));


    }

    @Test
    public void PurgingIsOverTest(){

        //DatalogMTL Program

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();
        OBDADataFactory odf = DATA_FACTORY;

        TemporalRange rangeLRS = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT1M"));
        TemporalRange rangeHRS = f.createTemporalRange(false, true, Duration.parse("PT0S"), Duration.parse("PT30S"));
        TemporalRange rangeMFON = f.createTemporalRange(false, true, Duration.parse("PT0S"), Duration.parse("PT10S"));
        TemporalRange rangeDiamondInner = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT2M"));
        TemporalRange rangeDiamondOuter = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT10M"));

        final Predicate conceptLRS = odf.getAtomPredicate("LowRotorSpeed", 1);
        final Predicate conceptHRS = odf.getAtomPredicate("HighRotorSpeed", 1);
        final Predicate conceptMFON = odf.getAtomPredicate("MainFlameOn", 1);
        final Predicate dataPropertyRs = odf.getAtomPredicate("rotorSpeed", 2);
        final Predicate conceptPIO = odf.getAtomPredicate("PurgingIsOver", 1);

        final Predicate conceptTurbine = odf.getAtomPredicate("Turbine", 1);
        final Predicate conceptTempSensor = odf.getAtomPredicate("TemperatureSensor",1);
        final Predicate conceptRotSpeedSensor = odf.getAtomPredicate("RotationSpeedSensor", 1);
        final Predicate objectPropertyIMB = odf.getAtomPredicate("isMonitoredBy", 2);
        final Predicate objectPropertyIPO = odf.getAtomPredicate("isPartOf", 2);
        final Predicate conceptCLTRS = odf.getAtomPredicate("ColocTempRotSensors", 3);

        final Variable varRs = odf.getVariable("rs");
        final Variable varTs = odf.getVariable("ts");
        final Variable varTb = odf.getVariable("tb");
        final Variable varV = odf.getVariable("v");
        final Variable varPt = odf.getVariable("pt");
        final Variable varBurner = odf.getVariable("b");

        TemporalAtomicExpression lrs = f.createTemporalAtomicExpression(conceptLRS, varRs);
        TemporalAtomicExpression rs = f.createTemporalAtomicExpression(dataPropertyRs, varTb, varV);
        TemporalAtomicExpression comparisonLs = f.createTemporalAtomicExpression(ExpressionOperation.LT, varV, odf.getConstantLiteral("1000", Predicate.COL_TYPE.DECIMAL));
        TemporalAtomicExpression hrs = f.createTemporalAtomicExpression(conceptHRS, varRs);
        TemporalAtomicExpression comparisonHs = f.createTemporalAtomicExpression(ExpressionOperation.GT, varV, odf.getConstantLiteral("1260", Predicate.COL_TYPE.DECIMAL));
        TemporalAtomicExpression mfon = f.createTemporalAtomicExpression(conceptMFON, varTs);
        TemporalAtomicExpression pio = f.createTemporalAtomicExpression(conceptPIO, varTb);

        TemporalAtomicExpression tb = f.createTemporalAtomicExpression(conceptTurbine, varTb);
        TemporalAtomicExpression ts = f.createTemporalAtomicExpression(conceptTempSensor, varTs);
        TemporalAtomicExpression rss = f.createTemporalAtomicExpression(conceptRotSpeedSensor, varRs);
        TemporalAtomicExpression isMonitoredByTs = f.createTemporalAtomicExpression(objectPropertyIMB, varBurner, varTs);
        TemporalAtomicExpression isMonitoredByRS = f.createTemporalAtomicExpression(objectPropertyIMB, varPt, varRs);
        TemporalAtomicExpression isPartOfPt = f.createTemporalAtomicExpression(objectPropertyIPO, varPt, varTb);
        TemporalAtomicExpression isPartOfB = f.createTemporalAtomicExpression(objectPropertyIPO, varBurner, varTb);
        TemporalAtomicExpression CLTRS = f.createTemporalAtomicExpression(conceptCLTRS, varTb, varTs, varRs);

        TemporalExpression boxMinusLRS = f.createBoxMinusExpression(rangeLRS,lrs);
        TemporalExpression diamondMinusLRS = f.createDiamondMinusExpression(rangeDiamondInner, boxMinusLRS);
        TemporalExpression boxMinusHRS = f.createBoxMinusExpression(rangeHRS, hrs);
        TemporalExpression innerExp = f.createTemporalJoinExpression(boxMinusHRS, diamondMinusLRS);
        TemporalExpression diamondInnerExp = f.createDiamondMinusExpression(rangeDiamondOuter, innerExp);
        TemporalExpression boxMinusMFON = f.createBoxMinusExpression(rangeMFON, mfon);
        TemporalExpression temporalPIO = f.createTemporalJoinExpression(boxMinusMFON, diamondInnerExp);
        TemporalExpression bodyCLTRS = f.createTemporalJoinExpression(tb, ts, rss, isMonitoredByRS, isMonitoredByTs, isPartOfPt, isPartOfB);
        TemporalExpression bodyPIO = f.createTemporalJoinExpression(temporalPIO, CLTRS);

        DatalogMTLRule CLTRSrule = f.createRule(CLTRS, bodyCLTRS);
        DatalogMTLRule LRSrule = f.createRule(lrs, f.createTemporalJoinExpression(rs, comparisonLs));
        DatalogMTLRule HRSrule = f.createRule(hrs, f.createTemporalJoinExpression(rs, comparisonHs));
        DatalogMTLRule PIOrule = f.createRule(pio, bodyPIO);

        DatalogMTLProgram program = f.createProgram(LRSrule, HRSrule, CLTRSrule, PIOrule);

        System.out.println(program.render());

        //Static Mappings





        //Temporal Mappings


    }

//    @Test
//    public void testTMappings() throws Exception {
//
//        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
//        String username = "postgres";
//        String password = "postgres";
//
//        Properties pref = new Properties();
//
//        QuestOWLFactory factory = new QuestOWLFactory();
//        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
//                .nativeOntopMappingFile("src/test/resources/siemens.obda")
//                .ontologyFile("src/test/resources/siemens.owl")
//                .properties(pref)
//                .jdbcUrl(url)
//                .jdbcUser(username)
//                .jdbcPassword(password)
//                .enableExistentialReasoning(true)
//                .enableTestMode()
//                .build();
//        QuestOWL reasoner = factory.createReasoner(config);
//    }

}
