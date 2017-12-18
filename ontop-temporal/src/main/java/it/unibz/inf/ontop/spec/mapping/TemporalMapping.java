package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomPredicate;

public interface TemporalMapping {
    MappingMetadata getMetadata();

    //Optional<IntermediateQuery> getDefinition(AtomPredicate predicate1, AtomPredicate predicate2);

    ImmutableSet<AtomPredicate> getPredicates();

    ImmutableMap<AtomPredicate, QuadrupleDefinition> getDefinitions();

    /**
     * TODO: remove it when the conversion to Datalog will not be needed anymore
     */
    ExecutorRegistry getExecutorRegistry();

}
