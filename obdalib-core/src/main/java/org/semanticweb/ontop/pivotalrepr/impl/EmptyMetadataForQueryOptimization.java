package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.pivotalrepr.MetadataForQueryOptimization;

/**
 * Provides no metadata
 */
public class EmptyMetadataForQueryOptimization implements MetadataForQueryOptimization {

    @Override
    public ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> getPrimaryKeys() {
        return ImmutableMultimap.of();
    }
}
