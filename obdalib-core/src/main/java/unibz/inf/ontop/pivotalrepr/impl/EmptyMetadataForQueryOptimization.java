package unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;
import unibz.inf.ontop.model.AtomPredicate;

/**
 * Provides no metadata
 */
public class EmptyMetadataForQueryOptimization implements MetadataForQueryOptimization {

    @Override
    public ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> getPrimaryKeys() {
        return ImmutableMultimap.of();
    }
}
