package it.unibz.inf.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.UriTemplateMatcher;

/**
 * TODO: explain
 *
 * IMMUTABLE
 *
 */
public interface MetadataForQueryOptimization {

    ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> getUniqueConstraints();

    DBMetadata getDBMetadata();
    
}
