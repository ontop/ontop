package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.OntopModelSingletons;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.temporal.model.*;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


public class DatalogMTLFactoryImplTest {

    @Test
    public void test() {

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();

        TemporalRange range1 = f.createTemporalRange(false, true, Duration.parse("PT20.345S"), Duration.parse("PT1H1M"));

        TemporalRange range2 = f.createTemporalRange(true, true, Duration.parse("PT20.345S"), Duration.parse("PT1H1M"));

        TermFactory odf = OntopModelSingletons.TERM_FACTORY;
        AtomFactory af = OntopModelSingletons.ATOM_FACTORY;

        final AtomPredicate p1 = af.getAtomPredicate("P1",1);
        final AtomPredicate p2 = af.getAtomPredicate("P2",2);
        final AtomPredicate p3 = af.getAtomPredicate("P3",1);
        final AtomPredicate p4 = af.getAtomPredicate("P4",1);

        final Variable v1 = odf.getVariable("v1");
        final Variable v2 = odf.getVariable("v2");
        final Variable v3 = odf.getVariable("v3");
        final Variable v4 = odf.getVariable("v4");

        TemporalAtomicExpression head = f.createTemporalAtomicExpression(p4, v1);

        DatalogMTLExpression body = f.createTemporalJoinExpression(
                f.createTemporalAtomicExpression(p2, v2, v3),
                f.createSinceExpression(
                        range1,
                        f.createBoxMinusExpression(range2, f.createTemporalAtomicExpression(p1, v4)),
                        f.createTemporalAtomicExpression(p2, v4)
                )
        );

        DatalogMTLRule rule = f.createRule(head, body);

        Map<String, String> prefixes = new HashMap<>();

        prefixes.put("ss:", "http://siemens.com/ns#");
        prefixes.put("st", "http://siemens.com/temporal/ns#");
        prefixes.put("obda:", "https://w3id.org/obda/vocabulary#");

        final DatalogMTLProgram program = f.createProgram(prefixes, rule);

        System.out.println(program.render());

    }

    @Test
    public void test2() {

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();

        TemporalRange range1 = f.createTemporalRange(false, true, Duration.parse("PT20.345S"), Duration.parse("PT1H1M"));

        TemporalRange range2 = f.createTemporalRange(true, true, Duration.parse("PT20.345S"), Duration.parse("PT1H1M"));

        TermFactory odf = OntopModelSingletons.TERM_FACTORY;
        AtomFactory af = OntopModelSingletons.ATOM_FACTORY;

        final AtomPredicate p1 = af.getAtomPredicate("P1",1);
        final AtomPredicate p2 = af.getAtomPredicate("P2", 2);
        final AtomPredicate p3 = af.getAtomPredicate("P3", 1);
        final AtomPredicate p4 = af.getAtomPredicate("P4", 1);

        final Variable v1 = odf.getVariable("v1");
        final Variable v2 = odf.getVariable("v2");
        final Variable v3 = odf.getVariable("v3");
        final Variable v4 = odf.getVariable("v4");

        TemporalAtomicExpression head = f.createTemporalAtomicExpression(p4, v1);

        DatalogMTLExpression body = f.createTemporalJoinExpression(f.createDiamondPlusExpression(range1,
                f.createTemporalJoinExpression(f.createBoxPlusExpression(range2, f.createTemporalAtomicExpression(p1, v4)), f.createTemporalAtomicExpression(p2, v4))),f.createTemporalAtomicExpression(p3,v1));

        DatalogMTLRule rule = f.createRule(head, body);

        Map<String, String> prefixes = new HashMap<>();

//        PREFIX ss: <http://siemens.com/ns#>
//        PREFIX st: <http://siemens.com/temporal/ns#>
//        PREFIX obda:   <https://w3id.org/obda/vocabulary#>
        prefixes.put("ss:", "http://siemens.com/ns#");
        prefixes.put("st", "http://siemens.com/temporal/ns#");
        prefixes.put("obda:", "https://w3id.org/obda/vocabulary#");

        final DatalogMTLProgram program = f.createProgram(prefixes, rule);

        System.out.println(program.render());

    }


}
