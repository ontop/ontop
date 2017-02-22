package it.unibz.inf.ontop.temporal.model;

public interface BinaryTemporalExpression extends TemporalExpression{

    TemporalExpression getLeftOperand();

    TemporalExpression getRightOperand();
}
