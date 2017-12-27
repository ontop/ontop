package it.unibz.inf.ontop.temporal.model;

public interface UnaryTemporalExpression extends TemporalExpression {

    DatalogMTLExpression getOperand();
}
