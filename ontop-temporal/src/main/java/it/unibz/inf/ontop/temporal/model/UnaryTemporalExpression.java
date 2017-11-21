package it.unibz.inf.ontop.temporal.model;

public interface UnaryTemporalExpression extends DatalogMTLExpression {

    DatalogMTLExpression getOperand();
}
