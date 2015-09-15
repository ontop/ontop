package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import org.semanticweb.ontop.model.AtomPredicate;

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
