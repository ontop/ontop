package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;
import it.unibz.inf.ontop.temporal.model.UntilExpression;

public class UntilExpressionImpl extends AbstractBinaryTemporalExpressionWithRange implements UntilExpression{
    UntilExpressionImpl(TemporalRange range, TemporalExpression leftOperand, TemporalExpression rightOperand) {
        super(range, leftOperand, rightOperand);
    }

    @Override
    public String toString() {
        return String.format("UntilExpressionImpl{range = %s, leftOperand = %s, rightOperand = %s",
                getRange(), getLeftOperand(), getRightOperand());
    }

    @Override
    public String render() {
        return String.format("(%s Until %s %s)", getLeftOperand().render(), getRange(), getRightOperand().render());
    }
}
