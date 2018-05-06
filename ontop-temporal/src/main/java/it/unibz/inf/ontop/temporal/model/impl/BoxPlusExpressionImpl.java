package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.*;

public class BoxPlusExpressionImpl extends AbstractUnaryTemporalExpressionWithRange implements BoxPlusExpression {
    BoxPlusExpressionImpl(TemporalRange range, DatalogMTLExpression operand) {
        super(range, operand);
    }

    @Override
    public String toString() {
        return String.format("ALWAYS IN FUTURE %s {%s}", getRange(), getOperand());
    }
}
