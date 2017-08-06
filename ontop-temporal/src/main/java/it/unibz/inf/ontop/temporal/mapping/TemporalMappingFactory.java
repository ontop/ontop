package it.unibz.inf.ontop.temporal.mapping;

import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.List;

public interface TemporalMappingFactory {

    TemporalMappingTarget createTarget(Function objectAtom, Variable beginInclusive, Variable endInclusive, Variable begin, Variable end);

    TemporalMappingAxiom createMappingAxiom(String sourceSQL, List<TemporalMappingTarget> targets);

    TemporalMappingAxiom createMappingAxiom(String sourceSQL, TemporalMappingTarget... targets);
}
