package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.OntopModelSingletons;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.temporal.model.DatalogMTLFactory;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import it.unibz.inf.ontop.temporal.model.TemporalAtomicExpression;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalInterval;
import it.unibz.inf.ontop.temporal.model.TemporalRange;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;


public class DatalogMTLFactoryImplTest {

    @Test
    public void test() {

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();

        TemporalRange range1 = f.createTemporalRange(false, true, Duration.parse("PT20.345S"), Duration.parse("PT1H1M"));

        TemporalRange range2 = f.createTemporalRange(true, true, Duration.parse("PT20.345S"), Duration.parse("PT1H1M"));

        TermFactory odf = OntopModelSingletons.TERM_FACTORY;

        final Predicate p1 = odf.getClassPredicate("P1");
        final Predicate p2 = odf.getObjectPropertyPredicate("P2");
        final Predicate p3 = odf.getClassPredicate("P3");
        final Predicate p4 = odf.getClassPredicate("P4");

        final Variable v1 = odf.getVariable("v1");
        final Variable v2 = odf.getVariable("v2");
        final Variable v3 = odf.getVariable("v3");
        final Variable v4 = odf.getVariable("v4");

        TemporalAtomicExpression head = f.createTemporalAtomicExpression(p4, v1);

        TemporalExpression body = f.createTemporalJoinExpression(
                f.createTemporalAtomicExpression(p2, v2, v3),
                f.createSinceExpression(
                        range1,
                        f.createBoxMinusExpression(range2, f.createTemporalAtomicExpression(p1, v4)),
                        f.createTemporalAtomicExpression(p2, v4)
                )
        );

        DatalogMTLRule rule = f.createRule(head, body);

        final DatalogMTLProgram program = f.createProgram(rule);

        System.out.println(program.render());

    }

    @Test
    public void test2() {

        DatalogMTLFactory f = DatalogMTLFactoryImpl.getInstance();

        TemporalRange range1 = f.createTemporalRange(false, true, Duration.parse("PT20.345S"), Duration.parse("PT1H1M"));

        TemporalRange range2 = f.createTemporalRange(true, true, Duration.parse("PT20.345S"), Duration.parse("PT1H1M"));

        TermFactory odf = OntopModelSingletons.TERM_FACTORY;

        final Predicate p1 = odf.getClassPredicate("P1");
        final Predicate p2 = odf.getPredicate("P2", 2);
        final Predicate p3 = odf.getPredicate("P3", 1);
        final Predicate p4 = odf.getPredicate("P4", 1);

        final Variable v1 = odf.getVariable("v1");
        final Variable v2 = odf.getVariable("v2");
        final Variable v3 = odf.getVariable("v3");
        final Variable v4 = odf.getVariable("v4");

        TemporalAtomicExpression head = f.createTemporalAtomicExpression(p4, v1);

        TemporalExpression body = f.createTemporalJoinExpression(f.createDiamondPlusExpression(range1,
                f.createTemporalJoinExpression(f.createBoxPlusExpression(range2, f.createTemporalAtomicExpression(p1, v4)), f.createTemporalAtomicExpression(p2, v4))),f.createTemporalAtomicExpression(p3,v1));

        DatalogMTLRule rule = f.createRule(head, body);

        final DatalogMTLProgram program = f.createProgram(rule);

        System.out.println(program.render());

    }


}
