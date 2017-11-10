package it.unibz.inf.ontop.iq.tools;


import it.unibz.inf.ontop.iq.node.ImmutableQueryModifiers;
import it.unibz.inf.ontop.iq.IntermediateQuery;

import java.util.Collection;
import java.util.Optional;

public interface UnionBasedQueryMerger {

    Optional<IntermediateQuery> mergeDefinitions(Collection<IntermediateQuery> predicateDefinitions);

    /**
     * TODO: describe
     * The optional modifiers are for the top construction node above the UNION (if any).
     */
    Optional<IntermediateQuery> mergeDefinitions(Collection<IntermediateQuery> predicateDefinitions,
                                                 ImmutableQueryModifiers topModifiers);
}
