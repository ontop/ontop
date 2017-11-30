package it.unibz.inf.ontop.temporal.model;


import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface DatalogMTLFactory {

    TemporalAtomicExpression createTemporalAtomicExpression(Predicate predicate, List<Term> terms);

    TemporalAtomicExpression createTemporalAtomicExpression(Predicate predicate, Term... terms);

    TemporalJoinExpression createTemporalJoinExpression(DatalogMTLExpression... expressions);

    TemporalJoinExpression createTemporalJoinExpression(List<DatalogMTLExpression> expressions);

    BoxPlusExpression createBoxPlusExpression(TemporalRange range, DatalogMTLExpression expression);

    BoxMinusExpression createBoxMinusExpression(TemporalRange range, DatalogMTLExpression expression);

    DiamondPlusExpression createDiamondPlusExpression(TemporalRange range, DatalogMTLExpression expression);

    DiamondMinusExpression createDiamondMinusExpression(TemporalRange range, DatalogMTLExpression expression);

    SinceExpression createSinceExpression(TemporalRange range, DatalogMTLExpression left, DatalogMTLExpression right);

    UntilExpression createUntilExpression(TemporalRange range, DatalogMTLExpression left, DatalogMTLExpression right);

    DatalogMTLRule createRule(AtomicExpression head, DatalogMTLExpression body);

    DatalogMTLProgram createProgram(Map<String, String> prefixes, List<DatalogMTLRule> rules);

    DatalogMTLProgram createProgram(Map<String, String> prefixes, DatalogMTLRule... rules);

    TemporalRange createTemporalRange(boolean beginInclusive, boolean endInclusive, Duration begin, Duration end);

    TemporalInterval createTemporalInterval(boolean beginInclusive, boolean endInclusive, Instant begin, Instant end);

    StaticAtomicExpression createStaticAtomicExpression(Predicate predicate, List<Term> terms);

    StaticAtomicExpression createStaticAtomicExpression(Predicate predicate, Term... terms);

    StaticJoinExpression createStaticJoinExpression(StaticExpression... expressions);

    StaticJoinExpression createStaticJoinExpression(List<StaticExpression> expressions);
}
