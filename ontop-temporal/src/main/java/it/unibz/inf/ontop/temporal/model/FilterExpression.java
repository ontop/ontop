package it.unibz.inf.ontop.temporal.model;

public interface FilterExpression extends DatalogMTLExpression {

    ComparisonExpression getComparisonExpression();

    //TODO: find a better name
    DatalogMTLExpression getExpression();
}
