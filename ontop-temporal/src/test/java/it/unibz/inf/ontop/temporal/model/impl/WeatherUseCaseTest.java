package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.temporal.model.*;
import org.junit.Test;

import java.time.Duration;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class WeatherUseCaseTest {

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

    @Test
    public void HeatAffectedCountyTest() {

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();

        TemporalRange range = f.createTemporalRange(true, true, Duration.parse("PT0H"), Duration.parse("PT24H"));

        OBDADataFactory odf = DATA_FACTORY;

        final Predicate p1 = odf.getPredicate("TempAbove24", 1);
        final Predicate p2 = odf.getPredicate("TempAbove41", 1);
        final Predicate p3 = odf.getPredicate("ExcessiveHeat", 1);
        final Predicate p4 = odf.getPredicate("LocatedInCounty", 2);
        final Predicate p5 = odf.getPredicate("HeatAffectedCounty", 2);

        final Variable v1 = odf.getVariable("SID");
        final Variable v2 = odf.getVariable("county");

        TemporalExpression excessiveHeat = f.createTemporalAtomicExpression(p3, v1);

        TemporalExpression head = f.createBoxMinusExpression(range, excessiveHeat);

        TemporalExpression body = f.createTemporalJoinExpression(
                f.createBoxMinusExpression(range, f.createTemporalAtomicExpression(p1,v1)),
                f.createDiamondMinusExpression(range, f.createTemporalAtomicExpression(p2,v1)));

        DatalogMTLRule rule1 = f.createRule(head, body);

        head = f.createTemporalAtomicExpression(p5, v2);

        body = f.createTemporalJoinExpression(f.createTemporalAtomicExpression(p4,v1,v2), excessiveHeat);

        DatalogMTLRule rule2 = f.createRule(head, body);

        final DatalogMTLProgram program = f.createProgram(rule1, rule2);

        System.out.println(program.render());

    }

    @Test
    public void ShoweryPatternCountyTest() {

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();

        TemporalRange range = f.createTemporalRange(true, true, Duration.parse("PT0M"), Duration.parse("PT30M"));

        OBDADataFactory odf = DATA_FACTORY;

        final Predicate p1 = odf.getPredicate("Precipitation", 1);
        final Predicate p2 = odf.getPredicate("NoPrecipitation", 1);
        final Predicate p3 = odf.getPredicate("LocatedInCounty", 2);
        final Predicate p4 = odf.getPredicate("ShoweryPatternCounty", 1);

        final Variable v1 = odf.getVariable("SID1");
        final Variable v2 = odf.getVariable("SID2");
        final Variable v3 = odf.getVariable("county");

        TemporalExpression head = f.createTemporalAtomicExpression(p4, v3);

        TemporalExpression body = f.createTemporalJoinExpression(f.createTemporalAtomicExpression(p3, v1, v3),
                f.createTemporalAtomicExpression(p3, v2, v3),
                f.createTemporalAtomicExpression(p1,v1),
                f.createTemporalAtomicExpression(p2, v2),
                f.createDiamondMinusExpression(range,f.createTemporalAtomicExpression(p1,v2)));

        DatalogMTLRule rule = f.createRule(head, body);

        final DatalogMTLProgram program = f.createProgram(rule);

        System.out.println(program.render());

    }

    @Test
    public void CyclonePatternStateTest() {

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();

        OBDADataFactory odf = DATA_FACTORY;

        final Predicate p1 = odf.getPredicate("CyclonePatternState", 1);
        final Predicate p2 = odf.getPredicate("LocatedInState", 2);
        final Predicate p3 = odf.getPredicate("NorthWind", 1);
        final Predicate p4 = odf.getPredicate("EastWind", 1);
        final Predicate p5 = odf.getPredicate("SouthWind", 1);
        final Predicate p6 = odf.getPredicate("WestWind", 1);

        final Variable v1 = odf.getVariable("SID1");
        final Variable v2 = odf.getVariable("SID2");
        final Variable v3 = odf.getVariable("SID3");
        final Variable v4 = odf.getVariable("SID4");
        final Variable v5 = odf.getVariable("state");

        TemporalExpression head = f.createTemporalAtomicExpression(p1, v5);

        TemporalExpression body = f.createTemporalJoinExpression(f.createTemporalAtomicExpression(p2,v1,v5), f.createTemporalAtomicExpression(p2,v2,v5), f.createTemporalAtomicExpression(p2,v3,v5),f.createTemporalAtomicExpression(p2,v4,v5),
                f.createTemporalAtomicExpression(p3,v1),f.createTemporalAtomicExpression(p4,v2),f.createTemporalAtomicExpression(p5,v3),f.createTemporalAtomicExpression(p6,v4));

        DatalogMTLRule rule = f.createRule(head, body);

        final DatalogMTLProgram program = f.createProgram(rule);

        System.out.println(program.render());

    }
}
