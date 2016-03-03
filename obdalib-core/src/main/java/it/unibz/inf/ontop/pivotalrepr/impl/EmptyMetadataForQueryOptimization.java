package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.UriTemplateMatcher;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;
import it.unibz.inf.ontop.model.AtomPredicate;

/**
 * Provides no metadata
 */
public class EmptyMetadataForQueryOptimization implements MetadataForQueryOptimization {

    @Override
    public ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> getPrimaryKeys() {
        return ImmutableMultimap.of();
    }

    @Override
    public UriTemplateMatcher getUriTemplateMatcher() {
        return new UriTemplateMatcher();
    }
}
