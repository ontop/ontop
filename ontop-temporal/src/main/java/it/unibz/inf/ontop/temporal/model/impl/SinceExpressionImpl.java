package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.SinceExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;

public class SinceExpressionImpl extends AbstractBinaryTemporalExpressionWithRange implements SinceExpression{
    SinceExpressionImpl(TemporalRange range, DatalogMTLExpression leftOperand, DatalogMTLExpression rightOperand) {
        super(range, leftOperand, rightOperand);
    }

    @Override
    public String toString() {
        return String.format("SinceExpressionImpl{range = %s, leftOperand = %s, rightOperand = %s",
                getRange(), getLeftOperand(), getRightOperand());
    }
}
