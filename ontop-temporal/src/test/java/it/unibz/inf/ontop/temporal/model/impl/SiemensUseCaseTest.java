package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.temporal.model.*;
import org.junit.Test;

import java.time.Duration;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class SiemensUseCaseTest {

    @Test
    public void PurgingIsOverTest(){

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();
        OBDADataFactory odf = DATA_FACTORY;

        TemporalRange rangeLRS = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT1M"));
        TemporalRange rangeHRS = f.createTemporalRange(false, true, Duration.parse("PT0S"), Duration.parse("PT30S"));
        TemporalRange rangeMFON = f.createTemporalRange(false, true, Duration.parse("PT0S"), Duration.parse("PT10S"));
        TemporalRange rangeDiamondInner = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT2M"));
        TemporalRange rangeDiamondOuter = f.createTemporalRange(false, true, Duration.parse("PT0M"), Duration.parse("PT10M"));

        final Predicate predicateLRS = odf.getPredicate("LowRotorSpeed", 1);
        final Predicate predicateHRS = odf.getPredicate("HighRotorSpeed", 1);
        final Predicate predicateMFON = odf.getPredicate("MainFlameOn", 1);

        final Variable varRs = odf.getVariable("rs");
        final Variable varTs = odf.getVariable("ts");
        final Variable varTb = odf.getVariable("tb");



    }

    @Test
    public void HurricaneAffectedStateTest() {

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();

        TemporalRange range = f.createTemporalRange(true, true, Duration.parse("PT0H"), Duration.parse("PT1H"));

        OBDADataFactory odf = DATA_FACTORY;

        final Predicate p1 = odf.getPredicate("Hurricane", 1);
        final Predicate p2 = odf.getPredicate("HurricaneForceWind", 1);

        final Variable v1 = odf.getVariable("SID");

        TemporalExpression hurricane = f.createTemporalAtomicExpression(p1, v1);
        TemporalExpression hfw = f.createTemporalAtomicExpression(p2, v1);

        TemporalExpression head = f.createBoxMinusExpression(range, hurricane);

        TemporalExpression body = f.createBoxMinusExpression(range, hfw);

        DatalogMTLRule rule1 = f.createRule(head, body);

        final Predicate p3 = odf.getPredicate("LocatedInState", 2);
        final Predicate p4 = odf.getPredicate("HurricaneAffectedState", 1);

        final Variable v2 = odf.getVariable("state");

        head = f.createTemporalAtomicExpression(p4, v2);

        TemporalExpression locatedInState = f.createTemporalAtomicExpression(p3,v1,v2);

        body = f.createTemporalJoinExpression(locatedInState, f.createTemporalAtomicExpression(p1,v1));

        DatalogMTLRule rule2 = f.createRule(head, body);

        final DatalogMTLProgram program = f.createProgram(rule1, rule2);

        System.out.println(program.render());

    }

}
