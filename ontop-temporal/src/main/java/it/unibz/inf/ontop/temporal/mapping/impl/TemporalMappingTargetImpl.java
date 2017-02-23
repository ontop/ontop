package it.unibz.inf.ontop.temporal.mapping.impl;

import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingTarget;

public class TemporalMappingTargetImpl implements TemporalMappingTarget {

    private final Function objectAtom;

    private final Variable beginInclusive;
    private final Variable endInclusive;
    private final Variable begin;
    private final Variable end;

    public TemporalMappingTargetImpl(Function objectAtom, Variable beginInclusive, Variable endInclusive, Variable begin, Variable end) {
        this.objectAtom = objectAtom;
        this.beginInclusive = beginInclusive;
        this.endInclusive = endInclusive;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public Function getObjectAtom() {
        return objectAtom;
    }

    @Override
    public Variable getBeginInclusive() {
        return beginInclusive;
    }

    @Override
    public Variable getEndInclusive() {
        return endInclusive;
    }

    @Override
    public Variable getBegin() {
        return begin;
    }

    @Override
    public Variable getEnd() {
        return end;
    }
}
