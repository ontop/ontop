package it.unibz.inf.ontop.owlrefplatform.core.optimization.unfolding;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.IntermediateQueryOptimizer;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

/**
 * TODO: explain
 */
public interface QueryUnfolder extends IntermediateQueryOptimizer {
    ImmutableMap<AtomPredicate, IntermediateQuery> getMappingIndex();
}
