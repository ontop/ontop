package it.unibz.inf.ontop.mapping.transf.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingMergingException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.io.impl.SimplePrefixManager;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.mapping.transf.MappingMerger;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.UriTemplateMatcher;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.transform.QueryMerger;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;

public class MappingMergerImpl implements MappingMerger {

    private final SpecificationFactory specificationFactory;
    private final QueryMerger queryMerger;

    @Inject
    private MappingMergerImpl(SpecificationFactory specificationFactory, QueryMerger queryMerger) {
        this.specificationFactory = specificationFactory;
        this.queryMerger = queryMerger;
    }

    @Override
    public Mapping merge(Mapping ... mappings) {
       return merge(ImmutableSet.copyOf(mappings));
    }

    @Override
    public Mapping merge(ImmutableSet<Mapping> mappings) {

        if (mappings.isEmpty()) {
            throw new IllegalArgumentException("The set of mappings is assumed to be nonempty");
        }

        MappingMetadata metadata = mergeMetadata(mappings);
        ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap = mergeMappingMaps(mappings);

        // TODO: check that the ExecutorRegistry is identical for all mappings ?
        return specificationFactory.createMapping(
                metadata,
                mappingMap,
                mappings.iterator().next().getExecutorRegistry()
        );
    }

    private MappingMetadata mergeMetadata(ImmutableSet<Mapping> mappings) {

        PrefixManager prefixManager = mergePrefixManagers(mappings);
        UriTemplateMatcher uriTemplateMatcher = mergeURITemplateMatchers(mappings);
        return specificationFactory.createMetadata(prefixManager, uriTemplateMatcher);
    }

    private PrefixManager mergePrefixManagers(ImmutableSet<Mapping> mappings) {
        ImmutableMap<String, Collection<String>> prefixToUris = mappings.stream()
                .flatMap(m -> m.getMetadata().getPrefixManager().getPrefixMap().entrySet().stream())
                .collect(ImmutableCollectors.toMultimap())
                .asMap();

        ImmutableMap<String, String> prefixToUri = prefixToUris.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> e.getKey(),
                        e -> flattenURIList(e.getKey(), e.getValue())
                ));
        return new SimplePrefixManager(prefixToUri);
    }

    private String flattenURIList(String prefix, Collection<String> uris) {
        if (uris.size() == 1) {
            return uris.iterator().next();
        }
        throw new MappingMergingException("Conflicting URIs for prefix " + prefix + ": " + uris);
    }

    private UriTemplateMatcher mergeURITemplateMatchers(ImmutableSet<Mapping> mappings) {
        return UriTemplateMatcher.merge(
                mappings.stream()
                        .map(m -> m.getMetadata().getUriTemplateMatcher())
        );
    }

    private ImmutableMap<AtomPredicate, IntermediateQuery> mergeMappingMaps(ImmutableSet<Mapping> mappings) {

        ImmutableMap<AtomPredicate, Collection<IntermediateQuery>> atomPredicate2IQs = mappings.stream()
                .flatMap(m -> getMappingMap(m).entrySet().stream())
                .collect(ImmutableCollectors.toMultimap())
                .asMap();

        return atomPredicate2IQs.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> e.getKey(),
                        e -> queryMerger.mergeDefinitions(e.getValue())
                                .orElseThrow(() -> new MappingMergingException("The query should be present"))
                ));
    }

    private ImmutableMap<AtomPredicate, IntermediateQuery> getMappingMap(Mapping mapping) {
        return mapping.getPredicates().stream()
                .collect(ImmutableCollectors.toMap(
                        p -> p,
                        p -> mapping.getDefinition(p)
                                .orElseThrow(() -> new MappingMergingException("This atom predicate should have a definition"))
                ));
    }
}
