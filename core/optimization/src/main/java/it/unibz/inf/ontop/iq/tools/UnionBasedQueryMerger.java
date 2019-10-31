package it.unibz.inf.ontop.iq.tools;


import it.unibz.inf.ontop.iq.IQ;

import java.util.Collection;
import java.util.Optional;

/**
 * Accessible through Guice (recommended) or through OptimizationSingletons.
 */
public interface UnionBasedQueryMerger {

    Optional<IQ> mergeDefinitions(Collection<IQ> predicateDefinitions);
}
