package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.BinaryTemporalExpression;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;

import java.util.Arrays;

public abstract class AbstractBinaryTemporalExpressionWithRange extends AbstractTemporalExpressionWithRange implements BinaryTemporalExpression {

    private final DatalogMTLExpression leftOperand, rightOperand;

    AbstractBinaryTemporalExpressionWithRange(TemporalRange range, DatalogMTLExpression leftOperand, DatalogMTLExpression rightOperand) {
        super(range);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    @Override
    public Iterable<DatalogMTLExpression> getChildNodes() {
        return Arrays.asList(leftOperand, rightOperand);
    }

    @Override
    public DatalogMTLExpression getLeftOperand() {
        return leftOperand;
    }

    @Override
    public DatalogMTLExpression getRightOperand() {
        return rightOperand;
    }
}
