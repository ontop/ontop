package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.NAryTemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalJoinExpression;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class TemporalJoinExpressionImpl implements TemporalJoinExpression, NAryTemporalExpression {

    private final List<TemporalExpression> operands;

    TemporalJoinExpressionImpl(List<TemporalExpression> operands) {
        this.operands = operands;
    }

    TemporalJoinExpressionImpl(TemporalExpression... operands) {
        this.operands = Arrays.asList(operands);
    }


    @Override
    public List<TemporalExpression> getOperands() {
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
        return operands.stream().map(TemporalExpression::render).collect(joining(","));
    }
}
