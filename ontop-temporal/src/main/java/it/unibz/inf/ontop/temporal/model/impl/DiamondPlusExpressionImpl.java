package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DiamondPlusExpression;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;

public class DiamondPlusExpressionImpl extends AbstractUnaryTemporalExpressionWithRange implements DiamondPlusExpression {
    DiamondPlusExpressionImpl(TemporalRange range, TemporalExpression operand) {
        super(range, operand);
    }

    @Override
    public String render() {
        return String.format("<+> %s %s", getRange(), getOperand());
    }

    @Override
    public String toString() {
        return String.format("DiamondPlusExpressionImpl{range = %s, operand = %s}", getRange(), getOperand());
    }
}
