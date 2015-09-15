package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.pivotalrepr.MetadataForQueryOptimization;

public class MetadataForQueryOptimizationImpl implements MetadataForQueryOptimization {

    private final ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> primaryKeys;

    public MetadataForQueryOptimizationImpl(
            ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    @Override
    public ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> getPrimaryKeys() {
        return primaryKeys;
    }
}
