package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.SinceExpression;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;

public class SinceExpressionImpl extends AbstractBinaryTemporalExpressionWithRange implements SinceExpression{
    SinceExpressionImpl(TemporalRange range, TemporalExpression leftOperand, TemporalExpression rightOperand) {
        super(range, leftOperand, rightOperand);
    }

    @Override
    public String toString() {
        return String.format("SinceExpressionImpl{range = %s, leftOperand = %s, rightOperand = %s",
                getRange(), getLeftOperand(), getRightOperand());
    }

    @Override
    public String render() {
        return String.format("(%s Since %s %s)", getLeftOperand().render(), getRange(), getRightOperand().render());
    }
}
