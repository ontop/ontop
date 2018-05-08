package it.unibz.inf.ontop.temporal.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.temporal.model.DatalogMTLExpression;
import it.unibz.inf.ontop.temporal.model.StaticExpression;
import it.unibz.inf.ontop.temporal.model.StaticJoinExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StaticJoinExpressionImpl implements StaticJoinExpression {

    private final List<StaticExpression> operands;

    StaticJoinExpressionImpl(List<StaticExpression> operands) {
        this.operands = operands;
    }

    StaticJoinExpressionImpl(StaticExpression... operands) {
        this.operands = Arrays.asList(operands);
    }

    @Override
    public Iterable<StaticExpression> getChildNodes() {
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

    @Override
    public String toString() {
        StringBuilder s= new StringBuilder();
        for (DatalogMTLExpression expression : getOperands())
            s.append(expression).append(",\n\t");
        return s.toString();
    }

    @Override
    public List<StaticExpression> getOperands() {
        return operands;
    }

    @Override
    public int getArity() {
        return getOperands().size();
    }
}
