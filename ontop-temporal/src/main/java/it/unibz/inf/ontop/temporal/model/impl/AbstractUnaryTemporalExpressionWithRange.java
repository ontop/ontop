package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;
import it.unibz.inf.ontop.temporal.model.UnaryTemporalExpression;

public abstract class AbstractUnaryTemporalExpressionWithRange extends AbstractTemporalExpressionWithRange implements UnaryTemporalExpression  {

    private final TemporalExpression operand;

    AbstractUnaryTemporalExpressionWithRange(TemporalRange range, TemporalExpression operand){
        super(range);
        this.operand = operand;
    }

    @Override
    public TemporalExpression getOperand() {
        return operand;
    }
}
