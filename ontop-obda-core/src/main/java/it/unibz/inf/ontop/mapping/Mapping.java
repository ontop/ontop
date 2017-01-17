package it.unibz.inf.ontop.mapping;


import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface Mapping {

    MappingMetadata getMetadata();

    Optional<IntermediateQuery> getDefinition(AtomPredicate predicate);

}
