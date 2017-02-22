package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DiamondMinusExpression;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;

public class DiamondMinusExpressionImpl extends AbstractUnaryTemporalExpressionWithRange implements DiamondMinusExpression {
    DiamondMinusExpressionImpl(TemporalRange range, TemporalExpression operand) {
        super(range, operand);
    }

    @Override
    public String render() {
        return String.format("<-> %s %s", getRange(), getOperand());
    }

    @Override
    public String toString() {
        return String.format("DiamondMinusExpressionImpl{range = %s, operand = %s}", getRange(), getOperand());
    }
}
