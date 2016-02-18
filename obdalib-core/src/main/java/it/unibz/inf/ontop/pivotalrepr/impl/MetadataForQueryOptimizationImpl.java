package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;
import it.unibz.inf.ontop.model.AtomPredicate;

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
