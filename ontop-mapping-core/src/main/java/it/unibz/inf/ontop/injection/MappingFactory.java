package it.unibz.inf.ontop.injection;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.UriTemplateMatcher;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

import java.util.stream.Stream;

/**
 * To be build by Guice (Assisted inject pattern)
 */
public interface MappingFactory {

    PrefixManager create(ImmutableMap<String, String> prefixToURIMap);

    MappingMetadata create(PrefixManager prefixManager, UriTemplateMatcher templateMatcher);

    Mapping create(MappingMetadata metadata, Stream<IntermediateQuery> mappingStream);

    Mapping create(MappingMetadata metadata, ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap);
}
