package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.ComparisonExpression;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.FilterExpression;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class FilterExpressionImpl implements FilterExpression {
    private DatalogMTLExpression datalogMTLExpression;
    private ComparisonExpression comparisonExpression;

    FilterExpressionImpl(DatalogMTLExpression datalogMTLExpression, ComparisonExpression comparisonExpression) {
        this.datalogMTLExpression = datalogMTLExpression;
        this.comparisonExpression = comparisonExpression;
    }

    @Override
    public String toString() {
        return datalogMTLExpression.toString() +", "+comparisonExpression.toString();
    }

    @Override
    public String render() {
        return toString();
    }

    @Override
    public Iterable<DatalogMTLExpression> getChildNodes() {
        return Arrays.asList(datalogMTLExpression, comparisonExpression) ;
    }

    @Override
    public ComparisonExpression getComparisonExpression() {
        return this.comparisonExpression;
    }
}
