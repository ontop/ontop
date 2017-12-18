package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.TemporalJoinExpression;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class TemporalJoinExpressionImpl implements TemporalJoinExpression {

    private final List<DatalogMTLExpression> operands;

    TemporalJoinExpressionImpl(List<DatalogMTLExpression> operands) {
        this.operands = operands;
    }

    TemporalJoinExpressionImpl(DatalogMTLExpression... operands) {
        this.operands = Arrays.asList(operands);
    }


    @Override
    public List<DatalogMTLExpression> getOperands() {
        return operands;
    }

    @Override
    public String toString() {
        return "TemporalJoinExpressionImpl{" +
                "operands=" + operands +
                '}';
    }

    @Override
    public String render() {
        return operands.stream().map(DatalogMTLExpression::render).collect(joining(", "));
    }

    @Override
    public Iterable<DatalogMTLExpression> getChildNodes() {
        return operands;
    }
}
