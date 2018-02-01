package it.unibz.inf.ontop.temporal.mapping.impl;

import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;
import it.unibz.inf.ontop.temporal.model.term.BooleanConstant;

public class TemporalMappingIntervalImpl implements TemporalMappingInterval {

    private VariableOrGroundTerm beginInclusive;
    private VariableOrGroundTerm endInclusive;

    private Variable begin;
    private Variable end;

    @Override
    public VariableOrGroundTerm isBeginInclusive() {
        return beginInclusive;
    }

    @Override
    public VariableOrGroundTerm isEndInclusive() {
        return endInclusive;
    }

    @Override
    public String isBeginInclusiveToString() {
        return String.valueOf(beginInclusive);
    }

    @Override
    public String isEndInclusiveToString() {
        return String.valueOf(endInclusive);
    }

    @Override
    public Variable getBegin() {
        return begin;
    }

    @Override
    public Variable getEnd() {
        return end;
    }

    @Override
    public String toString() {
        if(beginInclusive instanceof BooleanConstant)
            return (((BooleanConstant)beginInclusive).getBooleanValue() ? "[" : "(") + begin + "," + end + (((BooleanConstant)endInclusive).getBooleanValue() ? "]" : ")");
        else
            return ((Variable)beginInclusive).getName() + " " + begin + "," + end + " " + ((Variable)endInclusive).getName();
    }

    public TemporalMappingIntervalImpl(VariableOrGroundTerm beginInclusive, VariableOrGroundTerm endInclusive, Variable begin, Variable end) {
        this.beginInclusive = beginInclusive;
        this.endInclusive = endInclusive;
        this.begin = begin;
        this.end = end;
    }

}
