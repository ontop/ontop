package it.unibz.inf.ontop.temporal.mapping.impl;

import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingAxiom;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingFactory;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingTarget;

import java.util.Arrays;
import java.util.List;

public class TemporalMappingFactoryImpl implements TemporalMappingFactory {


    @Override
    public TemporalMappingTarget createTarget(Function objectAtom, Variable beginInclusive, Variable endInclusive, Variable begin, Variable end) {
        //return new TemporalMappingTargetImpl(objectAtom, beginInclusive, endInclusive, begin, end);
        return null;
    }

    @Override
    public TemporalMappingAxiom createMappingAxiom(String sourceSQL, List<TemporalMappingTarget> targets) {
        //return new TemporalMappingAxiomImpl(sourceSQL, targets);
        return null;
    }

    @Override
    public TemporalMappingAxiom createMappingAxiom(String sourceSQL, TemporalMappingTarget... targets) {
        //return new TemporalMappingAxiomImpl(sourceSQL, Arrays.asList(targets));
        return null;
    }
}
