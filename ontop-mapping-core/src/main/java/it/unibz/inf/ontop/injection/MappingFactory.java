package it.unibz.inf.ontop.injection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.UriTemplateMatcher;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;

import java.util.stream.Stream;

/**
 * To be build by Guice (Assisted inject pattern)
 */
public interface MappingFactory {

    PrefixManager create(ImmutableMap<String, String> prefixToURIMap);

    MappingMetadata create(PrefixManager prefixManager, UriTemplateMatcher templateMatcher);

    Mapping create(MappingMetadata metadata, MetadataForQueryOptimization metadataForOptimization,
                   Stream<IntermediateQuery> mappingStream);

    Mapping create(MappingMetadata metadata, MetadataForQueryOptimization metadataForOptimization,
                   ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap);
}
