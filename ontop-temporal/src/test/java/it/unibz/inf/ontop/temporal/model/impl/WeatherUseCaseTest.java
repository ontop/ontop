package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.OntopModelSingletons;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.temporal.model.*;
import org.junit.Test;

import java.time.Duration;


public class WeatherUseCaseTest {

    @Test
    public void HurricaneAffectedStateTest() {

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();

        TemporalRange range = f.createTemporalRange(true, true, Duration.parse("PT0H"), Duration.parse("PT1H"));

        TermFactory odf = OntopModelSingletons.TERM_FACTORY;

        final Predicate p1 = odf.getClassPredicate("Hurricane");
        final Predicate p2 = odf.getClassPredicate("HurricaneForceWind");

        final Variable v1 = odf.getVariable("SID");

        TemporalExpression hurricane = f.createTemporalAtomicExpression(p1, v1);
        TemporalExpression hfw = f.createTemporalAtomicExpression(p2, v1);

        TemporalExpression head = f.createBoxMinusExpression(range, hurricane);

        TemporalExpression body = f.createBoxMinusExpression(range, hfw);

        DatalogMTLRule rule1 = f.createRule(head, body);

        final Predicate p3 = odf.getObjectPropertyPredicate("LocatedInState");
        final Predicate p4 = odf.getClassPredicate("HurricaneAffectedState");

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

        TermFactory odf = OntopModelSingletons.TERM_FACTORY;

        final Predicate p1 = odf.getClassPredicate("TempAbove24");
        final Predicate p2 = odf.getClassPredicate("TempAbove41");
        final Predicate p3 = odf.getClassPredicate("ExcessiveHeat");
        final Predicate p4 = odf.getObjectPropertyPredicate("LocatedInCounty");
        final Predicate p5 = odf.getClassPredicate("HeatAffectedCounty");

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

        TermFactory odf = OntopModelSingletons.TERM_FACTORY;

        final Predicate p1 = odf.getClassPredicate("Precipitation");
        final Predicate p2 = odf.getClassPredicate("NoPrecipitation");
        final Predicate p3 = odf.getObjectPropertyPredicate("LocatedInCounty");
        final Predicate p4 = odf.getClassPredicate("ShoweryPatternCounty");

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

        TermFactory odf = OntopModelSingletons.TERM_FACTORY;

        final Predicate p1 = odf.getClassPredicate("CyclonePatternState");
        final Predicate p2 = odf.getObjectPropertyPredicate("LocatedInState");
        final Predicate p3 = odf.getClassPredicate("NorthWind");
        final Predicate p4 = odf.getClassPredicate("EastWind");
        final Predicate p5 = odf.getClassPredicate("SouthWind");
        final Predicate p6 = odf.getClassPredicate("WestWind");

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
