package it.unibz.inf.ontop.temporal.model;

public interface FilterExpression extends DatalogMTLExpression {

    public ComparisonExpression getComparisonExpression();
}
