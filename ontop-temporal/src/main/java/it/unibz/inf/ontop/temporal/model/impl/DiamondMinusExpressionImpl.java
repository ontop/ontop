package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.*;

public class DiamondMinusExpressionImpl extends AbstractUnaryTemporalExpressionWithRange implements DiamondMinusExpression {
    DiamondMinusExpressionImpl(TemporalRange range, DatalogMTLExpression operand) {
        super(range, operand);
    }

    @Override
    public String toString() {
        return String.format("SOMETIME IN PAST %s {%s}", getRange(), getOperand());
    }
}
