package it.unibz.inf.ontop.mapping;


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface Mapping {

    MappingMetadata getMetadata();

    Optional<IntermediateQuery> getDefinition(AtomPredicate predicate);

    ImmutableSet<AtomPredicate> getPredicates();

    ImmutableCollection<IntermediateQuery> getQueries();

    /**
     * TODO: remove it when the conversion to Datalog will not be needed anymore
     */
    ExecutorRegistry getExecutorRegistry();
}
