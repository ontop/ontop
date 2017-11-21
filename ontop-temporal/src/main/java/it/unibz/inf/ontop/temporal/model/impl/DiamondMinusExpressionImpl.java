package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.*;

public class DiamondMinusExpressionImpl extends AbstractUnaryTemporalExpressionWithRange implements DiamondMinusExpression {
    DiamondMinusExpressionImpl(TemporalRange range, DatalogMTLExpression operand) {
        super(range, operand);
    }

    @Override
    public String render() {
        if (getOperand() instanceof BinaryTemporalExpression || getOperand() instanceof TemporalJoinExpression)
            return String.format("<-> %s (%s)", getRange(), getOperand().render());

        return String.format("<-> %s %s", getRange(), getOperand().render());
    }

    @Override
    public String toString() {
        return String.format("DiamondMinusExpressionImpl{range = %s, operand = %s}", getRange(), getOperand());
    }
}
