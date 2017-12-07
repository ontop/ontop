package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomPredicate;

import java.util.Optional;

public interface TemporalMapping {
    MappingMetadata getMetadata();

    //Optional<IntermediateQuery> getDefinition(AtomPredicate predicate1, AtomPredicate predicate2);

    ImmutableSet<AtomPredicate> getPredicates();

    ImmutableCollection<IntermediateQuery> getQueries();

    /**
     * TODO: remove it when the conversion to Datalog will not be needed anymore
     */
    ExecutorRegistry getExecutorRegistry();

}
