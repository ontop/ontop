package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.temporal.model.BoxMinusExpression;
import it.unibz.inf.ontop.temporal.model.BoxPlusExpression;
import it.unibz.inf.ontop.temporal.model.DatalogMTLFactory;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import it.unibz.inf.ontop.temporal.model.DiamondMinusExpression;
import it.unibz.inf.ontop.temporal.model.DiamondPlusExpression;
import it.unibz.inf.ontop.temporal.model.SinceExpression;
import it.unibz.inf.ontop.temporal.model.TemporalAtomicExpression;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalInterval;
import it.unibz.inf.ontop.temporal.model.TemporalJoinExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;
import it.unibz.inf.ontop.temporal.model.UntilExpression;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class DatalogMTLFactoryImpl implements DatalogMTLFactory {

    public static DatalogMTLFactory getInstance() {
        return new DatalogMTLFactoryImpl();
    }

    @Override
    public TemporalAtomicExpression createTemporalAtomicExpression(Predicate predicate, List<Term> terms) {
        return new TemporalAtomicExpressionImpl(predicate, terms);
    }

    @Override
    public TemporalAtomicExpression createTemporalAtomicExpression(Predicate predicate, Term... terms) {
        return new TemporalAtomicExpressionImpl(predicate, terms);
    }

    @Override
    public TemporalJoinExpression createTemporalJoinExpression(TemporalExpression... expressions) {
        return new TemporalJoinExpressionImpl(expressions);
    }

    @Override
    public TemporalJoinExpression createTemporalJoinExpression(List<TemporalExpression> expressions) {
        return new TemporalJoinExpressionImpl(expressions);
    }

    @Override
    public BoxPlusExpression createBoxPlusExpression(TemporalRange range, TemporalExpression expression) {
        return new BoxPlusExpressionImpl(range, expression);
    }

    @Override
    public BoxMinusExpression createBoxMinusExpression(TemporalRange range, TemporalExpression expression) {
        return new BoxMinusExpressionImpl(range, expression);
    }

    @Override
    public DiamondPlusExpression createDiamondPlusExpression(TemporalRange range, TemporalExpression expression) {
        return new DiamondPlusExpressionImpl(range, expression);
    }

    @Override
    public DiamondMinusExpression createDiamondMinusExpression(TemporalRange range, TemporalExpression expression) {
        return new DiamondMinusExpressionImpl(range, expression);
    }

    @Override
    public SinceExpression createSinceExpression(TemporalRange range, TemporalExpression left, TemporalExpression right) {
        return new SinceExpressionImpl(range, left, right);
    }

    @Override
    public UntilExpression createUntilExpression(TemporalRange range, TemporalExpression left, TemporalExpression right) {
        return new UntilExpressionImpl(range, left, right);
    }

    @Override
    public DatalogMTLRule createRule(TemporalExpression head, TemporalExpression body) {
        return new DatalogMTLRuleImpl(head, body);
    }

    @Override
    public DatalogMTLProgram createProgram(List<DatalogMTLRule> rules) {
        return new DatalogMTLProgramImpl(rules);
    }


    @Override
    public DatalogMTLProgram createProgram(DatalogMTLRule... rules) {
        return new DatalogMTLProgramImpl(Arrays.asList(rules));
    }

    @Override
    public TemporalRange createTemporalRange(boolean beginInclusive, boolean endInclusive, Duration begin, Duration end) {
        return new TemporalRangeImpl(beginInclusive, endInclusive, begin, end);
    }

    @Override
    public TemporalInterval createTemporalInterval(boolean beginInclusive, boolean endInclusive, Instant begin, Instant end) {
        return new TemporalIntervalImpl(beginInclusive, endInclusive, begin, end);
    }
}
