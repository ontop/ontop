package it.unibz.inf.ontop.temporal.model;

public interface BinaryTemporalExpression extends TemporalExpression {

    DatalogMTLExpression getLeftOperand();

    DatalogMTLExpression getRightOperand();
}
