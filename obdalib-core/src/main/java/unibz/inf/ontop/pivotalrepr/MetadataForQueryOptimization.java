package unibz.inf.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import unibz.inf.ontop.model.AtomPredicate;

/**
 * TODO: explain
 *
 * IMMUTABLE
 *
 */
public interface MetadataForQueryOptimization {

    ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> getPrimaryKeys();

    /**
     * TODO: complete
     */
    
}
