package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.NAryDatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.StaticExpression;
import it.unibz.inf.ontop.temporal.model.StaticJoinExpression;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class StaticJoinExpressionImpl implements StaticJoinExpression {

    private final List<StaticExpression> operands;

    StaticJoinExpressionImpl(List<StaticExpression> operands) {
        this.operands = operands;
    }

    StaticJoinExpressionImpl(StaticExpression... operands) {
        this.operands = Arrays.asList(operands);
    }

    @Override
    public String render() {
        return operands.stream().map(StaticExpression::render).collect(joining(", "));
    }

    @Override
    public Iterable<StaticExpression> getChildNodes() {
        return operands;
    }

    @Override
    public List<StaticExpression> getOperands() {
        return operands;
    }
}
