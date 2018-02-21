package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingMergingException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.optimizer.MappingIQNormalizer;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.impl.SimplePrefixManager;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingMerger;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;

import java.util.Collection;
import java.util.Map;

public class MappingMergerImpl implements MappingMerger {

    private final SpecificationFactory specificationFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final MappingIQNormalizer mappingIQNormalizer;

    @Inject
    private MappingMergerImpl(SpecificationFactory specificationFactory, UnionBasedQueryMerger queryMerger,
                              MappingIQNormalizer mappingIQNormalizer) {
        this.specificationFactory = specificationFactory;
        this.queryMerger = queryMerger;
        this.mappingIQNormalizer = mappingIQNormalizer;
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
        if (ImmutableSet.copyOf(uris).size() == 1) {
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
                        Map.Entry::getKey,
                        e -> mergeDefinitions(e.getValue())
                ));
    }

    /**
     * Due to a Java compiler bug (hiding .orElseThrow() in a sub-method does the trick)
     */
    private IntermediateQuery mergeDefinitions(Collection<IntermediateQuery> queries) {
        return queryMerger.mergeDefinitions(queries)
                .map(mappingIQNormalizer::normalize)
                .orElseThrow(() -> new MappingMergingException("The query should be present"));
    }

    private ImmutableMap<AtomPredicate, IntermediateQuery> getMappingMap(Mapping mapping) {
        return mapping.getPredicates().stream()
                .collect(ImmutableCollectors.toMap(
                        p -> p,
                        p -> getDefinition(mapping, p)
                ));
    }

    /**
     * Due to a Java compiler bug (hiding .orElseThrow() in a sub-method does the trick)
     */
    private static IntermediateQuery getDefinition(Mapping mapping, AtomPredicate predicate) {
        return mapping.getDefinition(predicate)
                .orElseThrow(() -> new MappingMergingException("This atom predicate should have a definition"));
    }
}
