package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.BinaryTemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;
import it.unibz.inf.ontop.temporal.model.UnaryTemporalExpression;

public abstract class AbstractBinaryTemporalExpressionWithRange extends AbstractTemporalExpressionWithRange implements BinaryTemporalExpression {

    private final TemporalExpression leftOperand, rightOperand;

    AbstractBinaryTemporalExpressionWithRange(TemporalRange range, TemporalExpression leftOperand, TemporalExpression rightOperand) {
        super(range);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    @Override
    public TemporalExpression getLeftOperand() {
        return leftOperand;
    }

    @Override
    public TemporalExpression getRightOperand() {
        return rightOperand;
    }
}
