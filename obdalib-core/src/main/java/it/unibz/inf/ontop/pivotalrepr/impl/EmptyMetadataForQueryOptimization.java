package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.UriTemplateMatcher;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.sql.RDBMetadataExtractionTools;

/**
 * Provides no metadata
 */
public class EmptyMetadataForQueryOptimization implements MetadataForQueryOptimization {

    @Override
    public ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> getUniqueConstraints() {
        return ImmutableMultimap.of();
    }

    @Override
    public UriTemplateMatcher getUriTemplateMatcher() {
        return new UriTemplateMatcher();
    }

    @Override
    public DBMetadata getDBMetadata() {
        return RDBMetadataExtractionTools.createDummyMetadata();
    }
}
