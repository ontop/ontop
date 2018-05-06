package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.*;

public class DiamondPlusExpressionImpl extends AbstractUnaryTemporalExpressionWithRange implements DiamondPlusExpression {
    DiamondPlusExpressionImpl(TemporalRange range, DatalogMTLExpression operand) {
        super(range, operand);
    }

    @Override
    public String toString() {
        return String.format("SOMETIME IN FUTURE %s {%s}", getRange(), getOperand());
    }
}
