package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.impl.TestingTools;
import it.unibz.inf.ontop.temporal.model.*;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


public class WeatherUseCaseTest {

    @Test
    public void HurricaneAffectedStateTest() {

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();

        TemporalRange range = f.createTemporalRange(true, true, Duration.parse("PT0H"), Duration.parse("PT1H"));

        TermFactory odf = TestingTools.TERM_FACTORY;
        AtomFactory af = TestingTools.ATOM_FACTORY;

        final AtomPredicate p1 = af.getAtomPredicate("Hurricane",1);
        final AtomPredicate p2 = af.getAtomPredicate("HurricaneForceWind",1);

        final Variable v1 = odf.getVariable("SID");

        DatalogMTLExpression hurricane = f.createTemporalAtomicExpression(p1, v1);
        DatalogMTLExpression hfw = f.createTemporalAtomicExpression(p2, v1);

        TemporalAtomicExpression head = (TemporalAtomicExpression) f.createBoxMinusExpression(range, hurricane);

        DatalogMTLExpression body = f.createBoxMinusExpression(range, hfw);

        DatalogMTLRule rule1 = f.createRule(head, body);

        final AtomPredicate p3 =  af.getAtomPredicate("LocatedInState",2);
        final AtomPredicate p4 =  af.getAtomPredicate("HurricaneAffectedState",1);

        final Variable v2 = odf.getVariable("state");

        head = f.createTemporalAtomicExpression(p4, v2);

        DatalogMTLExpression locatedInState = f.createTemporalAtomicExpression(p3,v1,v2);

        body = f.createTemporalJoinExpression(locatedInState, f.createTemporalAtomicExpression(p1,v1));

        DatalogMTLRule rule2 = f.createRule(head, body);

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("mt", "http://mesowest.com/temporal/ns#");
        prefixes.put("obda:", "https://w3id.org/obda/vocabulary#");

        final DatalogMTLProgram program = f.createProgram(prefixes, rule1, rule2);

        System.out.println(program.render());

    }

//    @Test
//    public void HeatAffectedCountyTest() {
//
//        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();
//
//        TemporalRange range = f.createTemporalRange(true, true, Duration.parse("PT0H"), Duration.parse("PT24H"));
//
//        TermFactory odf = OntopModelSingletons.TERM_FACTORY;
//
//        final Predicate p1 = odf.getClassPredicate("TempAbove24");
//        final Predicate p2 = odf.getClassPredicate("TempAbove41");
//        final Predicate p3 = odf.getClassPredicate("ExcessiveHeat");
//        final Predicate p4 = odf.getObjectPropertyPredicate("LocatedInCounty");
//        final Predicate p5 = odf.getClassPredicate("HeatAffectedCounty");
//
//        final Variable v1 = odf.getVariable("SID");
//        final Variable v2 = odf.getVariable("county");
//
//        DatalogMTLExpression excessiveHeat = f.createTemporalAtomicExpression(p3, v1);
//
//        TemporalAtomicExpression head = (TemporalAtomicExpression) f.createBoxMinusExpression(range, excessiveHeat);
//
//        DatalogMTLExpression body = f.createTemporalJoinExpression(
//                f.createBoxMinusExpression(range, f.createTemporalAtomicExpression(p1,v1)),
//                f.createDiamondMinusExpression(range, f.createTemporalAtomicExpression(p2,v1)));
//
//        DatalogMTLRule rule1 = f.createRule(head, body);
//
//        head = f.createTemporalAtomicExpression(p5, v2);
//
//        body = f.createTemporalJoinExpression(f.createTemporalAtomicExpression(p4,v1,v2), excessiveHeat);
//
//        DatalogMTLRule rule2 = f.createRule(head, body);
//
//        Map<String, String> prefixes = new HashMap<>();
//        prefixes.put("mt", "http://mesowest.com/temporal/ns#");
//        prefixes.put("obda:", "https://w3id.org/obda/vocabulary#");
//
//        final DatalogMTLProgram program = f.createProgram(prefixes, rule1, rule2);
//
//        System.out.println(program.render());
//
//    }
//
//    @Test
//    public void ShoweryPatternCountyTest() {
//
//        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();
//
//        TemporalRange range = f.createTemporalRange(true, true, Duration.parse("PT0M"), Duration.parse("PT30M"));
//
//        TermFactory odf = OntopModelSingletons.TERM_FACTORY;
//
//        final Predicate p1 = odf.getClassPredicate("Precipitation");
//        final Predicate p2 = odf.getClassPredicate("NoPrecipitation");
//        final Predicate p3 = odf.getObjectPropertyPredicate("LocatedInCounty");
//        final Predicate p4 = odf.getClassPredicate("ShoweryPatternCounty");
//
//        final Variable v1 = odf.getVariable("SID1");
//        final Variable v2 = odf.getVariable("SID2");
//        final Variable v3 = odf.getVariable("county");
//
//        TemporalAtomicExpression head = f.createTemporalAtomicExpression(p4, v3);
//
//        DatalogMTLExpression body = f.createTemporalJoinExpression(f.createTemporalAtomicExpression(p3, v1, v3),
//                f.createTemporalAtomicExpression(p3, v2, v3),
//                f.createTemporalAtomicExpression(p1,v1),
//                f.createTemporalAtomicExpression(p2, v2),
//                f.createDiamondMinusExpression(range,f.createTemporalAtomicExpression(p1,v2)));
//
//        DatalogMTLRule rule = f.createRule(head, body);
//
//        Map<String, String> prefixes = new HashMap<>();
//        prefixes.put("mt", "http://mesowest.com/temporal/ns#");
//        prefixes.put("obda:", "https://w3id.org/obda/vocabulary#");
//
//        final DatalogMTLProgram program = f.createProgram(prefixes, rule);
//
//        System.out.println(program.render());
//
//    }
//
//    @Test
//    public void CyclonePatternStateTest() {
//
//        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();
//
//        TermFactory odf = OntopModelSingletons.TERM_FACTORY;
//
//        final Predicate p1 = odf.getClassPredicate("CyclonePatternState");
//        final Predicate p2 = odf.getObjectPropertyPredicate("LocatedInState");
//        final Predicate p3 = odf.getClassPredicate("NorthWind");
//        final Predicate p4 = odf.getClassPredicate("EastWind");
//        final Predicate p5 = odf.getClassPredicate("SouthWind");
//        final Predicate p6 = odf.getClassPredicate("WestWind");
//
//        final Variable v1 = odf.getVariable("SID1");
//        final Variable v2 = odf.getVariable("SID2");
//        final Variable v3 = odf.getVariable("SID3");
//        final Variable v4 = odf.getVariable("SID4");
//        final Variable v5 = odf.getVariable("state");
//
//        TemporalAtomicExpression head = f.createTemporalAtomicExpression(p1, v5);
//
//        DatalogMTLExpression body = f.createTemporalJoinExpression(f.createTemporalAtomicExpression(p2,v1,v5), f.createTemporalAtomicExpression(p2,v2,v5), f.createTemporalAtomicExpression(p2,v3,v5),f.createTemporalAtomicExpression(p2,v4,v5),
//                f.createTemporalAtomicExpression(p3,v1),f.createTemporalAtomicExpression(p4,v2),f.createTemporalAtomicExpression(p5,v3),f.createTemporalAtomicExpression(p6,v4));
//
//        DatalogMTLRule rule = f.createRule(head, body);
//
//        Map<String, String> prefixes = new HashMap<>();
//        prefixes.put("mt", "http://mesowest.com/temporal/ns#");
//        prefixes.put("obda:", "https://w3id.org/obda/vocabulary#");
//
//        final DatalogMTLProgram program = f.createProgram(prefixes, rule);
//
//        System.out.println(program.render());
//
//    }
}
