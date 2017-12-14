package it.unibz.inf.ontop.temporal.model;


import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface DatalogMTLFactory {

    TemporalAtomicExpression createTemporalAtomicExpression(AtomPredicate predicate, List<Term> terms);

    TemporalAtomicExpression createTemporalAtomicExpression(AtomPredicate predicate, Term... terms);

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

    StaticAtomicExpression createStaticAtomicExpression(AtomPredicate predicate, List<Term> terms);

    StaticAtomicExpression createStaticAtomicExpression(AtomPredicate predicate, Term... terms);

    StaticJoinExpression createStaticJoinExpression(StaticExpression... expressions);

    StaticJoinExpression createStaticJoinExpression(List<StaticExpression> expressions);

    ComparisonExpression createComparisonExpression(AtomPredicate predicate, VariableOrGroundTerm term1, VariableOrGroundTerm term2);
}
