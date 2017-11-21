package it.unibz.inf.ontop.temporal.model;

public interface BinaryTemporalExpression extends DatalogMTLExpression {

    DatalogMTLExpression getLeftOperand();

    DatalogMTLExpression getRightOperand();
}
