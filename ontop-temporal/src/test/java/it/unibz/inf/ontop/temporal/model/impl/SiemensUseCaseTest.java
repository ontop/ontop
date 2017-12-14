package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import junit.framework.TestCase;

import java.sql.Connection;


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

//    @Test
//    public void PurgingIsOverTest(){
//
//        //DatalogMTL Program
//
//        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();
//        TermFactory odf = OntopModelSingletons.TERM_FACTORY;
//
//        TemporalRange rangeLRS = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT1M"));
//        TemporalRange rangeHRS = f.createTemporalRange(false, true, Duration.parse("PT0S"), Duration.parse("PT30S"));
//        TemporalRange rangeMFON = f.createTemporalRange(false, true, Duration.parse("PT0S"), Duration.parse("PT10S"));
//        TemporalRange rangeDiamondInner = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT2M"));
//        TemporalRange rangeDiamondOuter = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT10M"));
//
//        final Predicate conceptLRS = odf.getClassPredicate("LowRotorSpeed");
//        final Predicate conceptHRS = odf.getClassPredicate("HighRotorSpeed");
//        final Predicate conceptMFON = odf.getClassPredicate("MainFlameOn");
//        final Predicate dataPropertyRs = odf.getObjectPropertyPredicate("rotorSpeed");
//        final Predicate conceptPIO = odf.getClassPredicate("PurgingIsOver");
//
//        final Predicate conceptTurbine = odf.getClassPredicate("Turbine");
//        final Predicate conceptTempSensor = odf.getClassPredicate("TemperatureSensor");
//        final Predicate conceptRotSpeedSensor = odf.getClassPredicate("RotationSpeedSensor");
//        final Predicate objectPropertyIMB = odf.getObjectPropertyPredicate("isMonitoredBy");
//        final Predicate objectPropertyIPO = odf.getObjectPropertyPredicate("isPartOf");
//        final Predicate conceptCLTRS = odf.getClassPredicate("ColocTempRotSensors");
//
//        final Variable varRs = odf.getVariable("rs");
//        final Variable varTs = odf.getVariable("ts");
//        final Variable varTb = odf.getVariable("tb");
//        final Variable varV = odf.getVariable("v");
//        final Variable varPt = odf.getVariable("pt");
//        final Variable varBurner = odf.getVariable("b");
//
//        TemporalAtomicExpression lrs = f.createTemporalAtomicExpression(conceptLRS, varRs);
//        TemporalAtomicExpression rs = f.createTemporalAtomicExpression(dataPropertyRs, varTb, varV);
//        TemporalAtomicExpression comparisonLs = f.createTemporalAtomicExpression(ExpressionOperation.LT, varV, odf.getConstantLiteral("1000", Predicate.COL_TYPE.DECIMAL));
//        TemporalAtomicExpression hrs = f.createTemporalAtomicExpression(conceptHRS, varRs);
//        TemporalAtomicExpression comparisonHs = f.createTemporalAtomicExpression(ExpressionOperation.GT, varV, odf.getConstantLiteral("1260", Predicate.COL_TYPE.DECIMAL));
//        TemporalAtomicExpression mfon = f.createTemporalAtomicExpression(conceptMFON, varTs);
//        TemporalAtomicExpression pio = f.createTemporalAtomicExpression(conceptPIO, varTb);
//
//        TemporalAtomicExpression tb = f.createTemporalAtomicExpression(conceptTurbine, varTb);
//        TemporalAtomicExpression ts = f.createTemporalAtomicExpression(conceptTempSensor, varTs);
//        TemporalAtomicExpression rss = f.createTemporalAtomicExpression(conceptRotSpeedSensor, varRs);
//        TemporalAtomicExpression isMonitoredByTs = f.createTemporalAtomicExpression(objectPropertyIMB, varBurner, varTs);
//        TemporalAtomicExpression isMonitoredByRS = f.createTemporalAtomicExpression(objectPropertyIMB, varPt, varRs);
//        TemporalAtomicExpression isPartOfPt = f.createTemporalAtomicExpression(objectPropertyIPO, varPt, varTb);
//        TemporalAtomicExpression isPartOfB = f.createTemporalAtomicExpression(objectPropertyIPO, varBurner, varTb);
//        TemporalAtomicExpression CLTRS = f.createTemporalAtomicExpression(conceptCLTRS, varTb, varTs, varRs);
//
//        DatalogMTLExpression boxMinusLRS = f.createBoxMinusExpression(rangeLRS,lrs);
//        DatalogMTLExpression diamondMinusLRS = f.createDiamondMinusExpression(rangeDiamondInner, boxMinusLRS);
//        DatalogMTLExpression boxMinusHRS = f.createBoxMinusExpression(rangeHRS, hrs);
//        DatalogMTLExpression innerExp = f.createTemporalJoinExpression(boxMinusHRS, diamondMinusLRS);
//        DatalogMTLExpression diamondInnerExp = f.createDiamondMinusExpression(rangeDiamondOuter, innerExp);
//        DatalogMTLExpression boxMinusMFON = f.createBoxMinusExpression(rangeMFON, mfon);
//        DatalogMTLExpression temporalPIO = f.createTemporalJoinExpression(boxMinusMFON, diamondInnerExp);
//        DatalogMTLExpression bodyCLTRS = f.createTemporalJoinExpression(tb, ts, rss, isMonitoredByRS, isMonitoredByTs, isPartOfPt, isPartOfB);
//        DatalogMTLExpression bodyPIO = f.createTemporalJoinExpression(temporalPIO, CLTRS);
//
//        DatalogMTLRule CLTRSrule = f.createRule(CLTRS, bodyCLTRS);
//        DatalogMTLRule LRSrule = f.createRule(lrs, f.createTemporalJoinExpression(rs, comparisonLs));
//        DatalogMTLRule HRSrule = f.createRule(hrs, f.createTemporalJoinExpression(rs, comparisonHs));
//        DatalogMTLRule PIOrule = f.createRule(pio, bodyPIO);
//
//        Map<String, String> prefixes = new HashMap<>();
//        prefixes.put("ss:", "http://siemens.com/ns#");
//        prefixes.put("st", "http://siemens.com/temporal/ns#");
//        prefixes.put("obda:", "https://w3id.org/obda/vocabulary#");
//
//        DatalogMTLProgram program = f.createProgram(prefixes, LRSrule, HRSrule, CLTRSrule, PIOrule);
//
//        System.out.println(program.render());
//
//        //Static Mappings
//
//
//        //Temporal Mappings
//
//
//    }

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
