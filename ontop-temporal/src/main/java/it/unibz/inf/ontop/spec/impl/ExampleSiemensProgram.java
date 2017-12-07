package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.model.OntopModelSingletons;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.temporal.model.impl.DatalogMTLFactoryImpl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ExampleSiemensProgram {

    public static DatalogMTLProgram getSampleProgram(){
        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();
        TermFactory odf = OntopModelSingletons.TERM_FACTORY;

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("ss:", "http://siemens.com/ns#");
        prefixes.put("st:", "http://siemens.com/temporal/ns#");
        prefixes.put("obda:", "https://w3id.org/obda/vocabulary#");

        TemporalRange rangeLRS = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT1M"));
        TemporalRange rangeHRS = f.createTemporalRange(false, true, Duration.parse("PT0S"), Duration.parse("PT30S"));
        TemporalRange rangeMFON = f.createTemporalRange(false, true, Duration.parse("PT0S"), Duration.parse("PT10S"));
        TemporalRange rangeDiamondInner = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT2M"));
        TemporalRange rangeDiamondOuter = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT10M"));

        final Predicate conceptLRS = odf.getClassPredicate(prefixes.get("st:") + "LowRotorSpeed");
        final Predicate conceptHRS = odf.getClassPredicate(prefixes.get("st:") + "HighRotorSpeed");
        final Predicate conceptMFON = odf.getClassPredicate(prefixes.get("st:") + "MainFlameOn");
        final Predicate dataPropertyRs = odf.getObjectPropertyPredicate(prefixes.get("st:") + "rotorSpeed");
        final Predicate conceptPIO = odf.getClassPredicate(prefixes.get("st:") + "PurgingIsOver");

        final Predicate conceptTurbine = odf.getClassPredicate(prefixes.get("ss:") + "Turbine");
        final Predicate conceptTempSensor = odf.getClassPredicate(prefixes.get("ss:") + "TemperatureSensor");
        final Predicate conceptRotSpeedSensor = odf.getClassPredicate(prefixes.get("ss:") + "RotationSpeedSensor");
        final Predicate objectPropertyIMB = odf.getObjectPropertyPredicate(prefixes.get("ss:") + "isMonitoredBy");
        final Predicate objectPropertyIPO = odf.getObjectPropertyPredicate(prefixes.get("ss:") + "isPartOf");
        final Predicate conceptCLTRS = odf.getClassPredicate(prefixes.get("ss:") + "ColocTempRotSensors");

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

        StaticAtomicExpression tb = f.createStaticAtomicExpression(conceptTurbine, varTb);
        StaticAtomicExpression ts = f.createStaticAtomicExpression(conceptTempSensor, varTs);
        StaticAtomicExpression rss = f.createStaticAtomicExpression(conceptRotSpeedSensor, varRs);
        StaticAtomicExpression isMonitoredByTs = f.createStaticAtomicExpression(objectPropertyIMB, varBurner, varTs);
        StaticAtomicExpression isMonitoredByRS = f.createStaticAtomicExpression(objectPropertyIMB, varPt, varRs);
        StaticAtomicExpression isPartOfPt = f.createStaticAtomicExpression(objectPropertyIPO, varPt, varTb);
        StaticAtomicExpression isPartOfB = f.createStaticAtomicExpression(objectPropertyIPO, varBurner, varTb);
        StaticAtomicExpression CLTRS = f.createStaticAtomicExpression(conceptCLTRS, varTb, varTs, varRs);

        DatalogMTLExpression boxMinusLRS = f.createBoxMinusExpression(rangeLRS,lrs);
        DatalogMTLExpression diamondMinusLRS = f.createDiamondMinusExpression(rangeDiamondInner, boxMinusLRS);
        DatalogMTLExpression boxMinusHRS = f.createBoxMinusExpression(rangeHRS, hrs);
        DatalogMTLExpression innerExp = f.createTemporalJoinExpression(boxMinusHRS, diamondMinusLRS);
        DatalogMTLExpression diamondInnerExp = f.createDiamondMinusExpression(rangeDiamondOuter, innerExp);
        DatalogMTLExpression boxMinusMFON = f.createBoxMinusExpression(rangeMFON, mfon);
        DatalogMTLExpression temporalPIO = f.createTemporalJoinExpression(boxMinusMFON, diamondInnerExp);
        StaticJoinExpression bodyCLTRS = f.createStaticJoinExpression(tb, ts, rss, isMonitoredByRS, isMonitoredByTs, isPartOfPt, isPartOfB);
        DatalogMTLExpression bodyPIO = f.createTemporalJoinExpression(temporalPIO, CLTRS);

        DatalogMTLRule CLTRSrule = f.createRule(CLTRS, bodyCLTRS);
        DatalogMTLRule LRSrule = f.createRule(lrs, f.createTemporalJoinExpression(rs, comparisonLs));
        DatalogMTLRule HRSrule = f.createRule(hrs, f.createTemporalJoinExpression(rs, comparisonHs));
        DatalogMTLRule PIOrule = f.createRule(pio, bodyPIO);


        return f.createProgram(prefixes, LRSrule, HRSrule, CLTRSrule, PIOrule);
    }
}
