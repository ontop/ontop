package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.BoxMinusExpression;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;

public class BoxMinusExpressionImpl extends AbstractUnaryTemporalExpressionWithRange implements BoxMinusExpression {
    BoxMinusExpressionImpl(TemporalRange range, TemporalExpression operand) {
        super(range, operand);
    }

    @Override
    public String render() {
        return String.format("⊟ %s %s", getRange(), getOperand().render());
    }

    @Override
    public String toString() {
        return String.format("BoxMinusExpressionImpl{range = %s, operand = %s}", getRange(), getOperand());
    }
}
