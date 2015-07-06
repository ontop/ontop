package org.semanticweb.ontop.owlrefplatform.core.optimization;

import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;

/**
 * TODO: explain
 */
public interface IntermediateQueryOptimizer {

    /**
     * TODO: explain
     */
    IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException;
}
