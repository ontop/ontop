package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.temporal.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DatalogMTLFactoryImpl implements DatalogMTLFactory {

    public static DatalogMTLFactory getInstance() {
        return new DatalogMTLFactoryImpl();
    }

    @Override
    public TemporalAtomicExpression createTemporalAtomicExpression(AtomPredicate predicate, List<Term> terms) {
        return new TemporalAtomicExpressionImpl(predicate, terms);
    }

    @Override
    public TemporalAtomicExpression createTemporalAtomicExpression(AtomPredicate predicate, Term... terms) {
        return new TemporalAtomicExpressionImpl(predicate, terms);
    }

    @Override
    public TemporalJoinExpression createTemporalJoinExpression(DatalogMTLExpression... expressions) {
        return new TemporalJoinExpressionImpl(expressions);
    }

    @Override
    public TemporalJoinExpression createTemporalJoinExpression(List<DatalogMTLExpression> expressions) {
        return new TemporalJoinExpressionImpl(expressions);
    }

    @Override
    public BoxPlusExpression createBoxPlusExpression(TemporalRange range, DatalogMTLExpression expression) {
        return new BoxPlusExpressionImpl(range, expression);
    }

    @Override
    public BoxMinusExpression createBoxMinusExpression(TemporalRange range, DatalogMTLExpression expression) {
        return new BoxMinusExpressionImpl(range, expression);
    }

    @Override
    public DiamondPlusExpression createDiamondPlusExpression(TemporalRange range, DatalogMTLExpression expression) {
        return new DiamondPlusExpressionImpl(range, expression);
    }

    @Override
    public DiamondMinusExpression createDiamondMinusExpression(TemporalRange range, DatalogMTLExpression expression) {
        return new DiamondMinusExpressionImpl(range, expression);
    }

    @Override
    public SinceExpression createSinceExpression(TemporalRange range, DatalogMTLExpression left, DatalogMTLExpression right) {
        return new SinceExpressionImpl(range, left, right);
    }

    @Override
    public UntilExpression createUntilExpression(TemporalRange range, DatalogMTLExpression left, DatalogMTLExpression right) {
        return new UntilExpressionImpl(range, left, right);
    }

    @Override
    public DatalogMTLRule createRule(AtomicExpression head, DatalogMTLExpression body) {
        return new DatalogMTLRuleImpl(head, body);
    }

    @Override
    public DatalogMTLProgram createProgram(Map<String, String> prefixes, List<DatalogMTLRule> rules) {
        return new DatalogMTLProgramImpl(prefixes, rules);
    }


    @Override
    public DatalogMTLProgram createProgram(Map<String, String> prefixes, DatalogMTLRule... rules) {
        return new DatalogMTLProgramImpl(prefixes, Arrays.asList(rules));
    }

    @Override
    public TemporalRange createTemporalRange(boolean beginInclusive, boolean endInclusive, Duration begin, Duration end) {
        return new TemporalRangeImpl(beginInclusive, endInclusive, begin, end);
    }

    @Override
    public TemporalInterval createTemporalInterval(boolean beginInclusive, boolean endInclusive, Instant begin, Instant end) {
        return new TemporalIntervalImpl(beginInclusive, endInclusive, begin, end);
    }

    @Override
    public StaticAtomicExpression createStaticAtomicExpression(AtomPredicate predicate, List<Term> terms) {
        return new StaticAtomicExpressionImpl(predicate, terms);
    }

    @Override
    public StaticAtomicExpression createStaticAtomicExpression(AtomPredicate predicate, Term... terms) {
        return new StaticAtomicExpressionImpl(predicate, terms);
    }

    @Override
    public StaticJoinExpression createStaticJoinExpression(StaticExpression... expressions) {
        return new StaticJoinExpressionImpl(expressions);
    }

    @Override
    public StaticJoinExpression createStaticJoinExpression(List<StaticExpression> expressions) {
        return new StaticJoinExpressionImpl(expressions);
    }

    @Override
    public ComparisonExpression createComparisonExpression(AtomPredicate predicate, VariableOrGroundTerm term1, VariableOrGroundTerm term2) {
        return new ComparisonExpressionImpl(predicate, term1, term2);
    }
}
