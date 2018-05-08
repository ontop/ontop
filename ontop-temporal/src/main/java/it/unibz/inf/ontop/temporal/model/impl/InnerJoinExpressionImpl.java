package it.unibz.inf.ontop.temporal.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.InnerJoinExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class InnerJoinExpressionImpl implements InnerJoinExpression{
    private final List<DatalogMTLExpression> operands;

    InnerJoinExpressionImpl(List<DatalogMTLExpression> operands) {
        this.operands = operands;
    }

    InnerJoinExpressionImpl(DatalogMTLExpression... operands) {
        this.operands = Arrays.asList(operands);
    }


    @Override
    public List<DatalogMTLExpression> getOperands() {
        return operands;
    }

    @Override
    public String toString() {
        StringBuilder s= new StringBuilder();
        for (DatalogMTLExpression expression : getOperands())
            s.append(expression).append("\n\t");
        return s.toString();
    }

    @Override
    public Iterable<DatalogMTLExpression> getChildNodes() {
        return operands;
    }

    @Override
    public ImmutableList<VariableOrGroundTerm> getAllVariableOrGroundTerms() {
        ArrayList<VariableOrGroundTerm> newList = new ArrayList<>();
        for (DatalogMTLExpression operand : operands){
            newList.addAll(operand.getAllVariableOrGroundTerms());
        }
        return  ImmutableList.copyOf(newList);
    }
}
