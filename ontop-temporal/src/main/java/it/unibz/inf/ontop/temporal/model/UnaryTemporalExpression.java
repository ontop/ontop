package it.unibz.inf.ontop.temporal.model;

public interface UnaryTemporalExpression extends TemporalExpression{

    TemporalExpression getOperand();
}
