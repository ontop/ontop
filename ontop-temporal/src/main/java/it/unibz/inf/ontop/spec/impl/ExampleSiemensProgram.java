package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.model.OntopModelSingletons;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.temporal.model.impl.DatalogMTLFactoryImpl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ExampleSiemensProgram {

    public static DatalogMTLProgram getSampleProgram(){
        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();
        AtomFactory af = OntopModelSingletons.ATOM_FACTORY;
        TermFactory tf = OntopModelSingletons.TERM_FACTORY;

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("ss:", "http://siemens.com/ns#");
        prefixes.put("st:", "http://siemens.com/temporal/ns#");
        prefixes.put("obda:", "https://w3id.org/obda/vocabulary#");

        TemporalRange rangeLRS = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT1M"));
        TemporalRange rangeHRS = f.createTemporalRange(false, true, Duration.parse("PT0S"), Duration.parse("PT30S"));
        TemporalRange rangeMFON = f.createTemporalRange(false, true, Duration.parse("PT0S"), Duration.parse("PT10S"));
        TemporalRange rangeDiamondInner = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT2M"));
        TemporalRange rangeDiamondOuter = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT10M"));

        final AtomPredicate conceptLRS = af.getAtomPredicate(prefixes.get("st:") + "LowRotorSpeed", 1);
        final AtomPredicate conceptHRS = af.getAtomPredicate(prefixes.get("st:") + "HighRotorSpeed",1);
        final AtomPredicate conceptMFON = af.getAtomPredicate(prefixes.get("st:") + "MainFlameOn",1);
        final AtomPredicate dataPropertyRs = af.getAtomPredicate(prefixes.get("st:") + "rotorSpeed",2);
        final AtomPredicate conceptPIO = af.getAtomPredicate(prefixes.get("st:") + "PurgingIsOver",1);

        final AtomPredicate conceptTurbine = af.getAtomPredicate(prefixes.get("ss:") + "Turbine",1);
        final AtomPredicate conceptTempSensor = af.getAtomPredicate(prefixes.get("ss:") + "TemperatureSensor",1);
        final AtomPredicate conceptRotSpeedSensor = af.getAtomPredicate(prefixes.get("ss:") + "RotationSpeedSensor",1);
        final AtomPredicate objectPropertyIMB = af.getAtomPredicate(prefixes.get("ss:") + "isMonitoredBy",2);
        final AtomPredicate objectPropertyIPO = af.getAtomPredicate(prefixes.get("ss:") + "isPartOf",2);
        final AtomPredicate conceptCLTRS = af.getAtomPredicate(prefixes.get("ss:") + "ColocTempRotSensors",2);

        final Variable varRs = tf.getVariable("rs");
        final Variable varTs = tf.getVariable("ts");
        final Variable varTb = tf.getVariable("tb");
        final Variable varV = tf.getVariable("v");
        final Variable varPt = tf.getVariable("pt");
        final Variable varBurner = tf.getVariable("b");

        TemporalAtomicExpression lrs = f.createTemporalAtomicExpression(conceptLRS, varRs);
        TemporalAtomicExpression rs = f.createTemporalAtomicExpression(dataPropertyRs, varRs, varV);
        ComparisonExpression comparisonLs = f.createComparisonExpression(af.getAtomPredicate("LT", 2), varV, tf.getConstantLiteral("1000", Predicate.COL_TYPE.DECIMAL));
        TemporalAtomicExpression hrs = f.createTemporalAtomicExpression(conceptHRS, varRs);
        ComparisonExpression comparisonHs = f.createComparisonExpression(af.getAtomPredicate("GT",2), varV, tf.getConstantLiteral("1260", Predicate.COL_TYPE.DECIMAL));
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
        DatalogMTLRule LRSrule = f.createRule(lrs, f.createFilterExpression(rs, comparisonLs));
        DatalogMTLRule HRSrule = f.createRule(hrs, f.createFilterExpression(rs, comparisonHs));
        DatalogMTLRule PIOrule = f.createRule(pio, bodyPIO);


        return f.createProgram(prefixes, LRSrule, HRSrule, CLTRSrule, PIOrule);
    }
}
