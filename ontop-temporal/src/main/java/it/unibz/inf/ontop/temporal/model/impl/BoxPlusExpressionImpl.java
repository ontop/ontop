package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.BoxPlusExpression;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;

public class BoxPlusExpressionImpl extends AbstractUnaryTemporalExpressionWithRange implements BoxPlusExpression {
    BoxPlusExpressionImpl(TemporalRange range, TemporalExpression operand) {
        super(range, operand);
    }

    @Override
    public String render() {
        return String.format("⊞ %s %s", getRange(), getOperand());
    }

    @Override
    public String toString() {
        return String.format("BoxPlusExpressionImpl{range = %s, operand = %s}", getRange(), getOperand());
    }
}
