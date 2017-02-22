package it.unibz.inf.ontop.temporal.model;

public interface BinaryTemporalExpression {

    TemporalExpression getLeftOperand();

    TemporalExpression getRightOperand();
}
