package it.unibz.inf.ontop.temporal.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.temporal.model.BinaryTemporalExpression;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.TemporalRange;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Override
    public ImmutableList<VariableOrGroundTerm> getAllVariableOrGroundTerms(){
        List<VariableOrGroundTerm> newList = new ArrayList<>();
        newList.addAll(leftOperand.getAllVariableOrGroundTerms());
        newList.addAll(rightOperand.getAllVariableOrGroundTerms());
        return ImmutableList.copyOf(newList);
    }
}
