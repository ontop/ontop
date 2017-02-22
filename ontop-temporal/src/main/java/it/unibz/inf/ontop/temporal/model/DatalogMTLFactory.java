package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.Term;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface DatalogMTLFactory {

    TemporalAtomicExpression createTemporalAtomicExpression(Predicate predicate, List<Term> terms);

    TemporalAtomicExpression createTemporalAtomicExpression(Predicate predicate, Term... terms);

    TemporalJoinExpression createTemporalJoinExpression(TemporalExpression... expressions);

    TemporalJoinExpression createTemporalJoinExpression(List<TemporalExpression> expressions);

    BoxPlusExpression createBoxPlusExpression(TemporalRange range, TemporalExpression expression);

    BoxMinusExpression createBoxMinusExpression(TemporalRange range, TemporalExpression expression);

    DiamondPlusExpression createDiamondPlusExpression(TemporalRange range, TemporalExpression expression);

    DiamondMinusExpression createDiamondMinusExpression(TemporalRange range, TemporalExpression expression);

    SinceExpression createSinceExpression(TemporalRange range, TemporalExpression left, TemporalExpression right);

    UntilExpression createUntilExpression(TemporalRange range, TemporalExpression left, TemporalExpression right);

    DatalogMTLRule createRule(TemporalExpression head, TemporalExpression body);

    DatalogMTLProgram createProgram(List<DatalogMTLRule> rules);

    DatalogMTLProgram createProgram(DatalogMTLRule... rules);

    TemporalRange createTemporalRange(boolean beginInclusive, boolean endInclusive, Duration begin, Duration end);

    TemporalInterval createTemporalInterval(boolean beginInclusive, boolean endInclusive, Instant begin, Instant end);
}
