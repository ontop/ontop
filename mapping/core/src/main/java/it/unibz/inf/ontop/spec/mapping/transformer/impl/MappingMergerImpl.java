package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingMergingException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.MappingIQNormalizer;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.impl.SimplePrefixManager;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingMerger;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.apache.commons.rdf.api.IRI;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class MappingMergerImpl implements MappingMerger {

    private final SpecificationFactory specificationFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final MappingIQNormalizer mappingIQNormalizer;
    private final TermFactory termFactory;

    @Inject
    private MappingMergerImpl(SpecificationFactory specificationFactory, UnionBasedQueryMerger queryMerger,
                              MappingIQNormalizer mappingIQNormalizer, TermFactory termFactory) {
        this.specificationFactory = specificationFactory;
        this.queryMerger = queryMerger;
        this.mappingIQNormalizer = mappingIQNormalizer;
        this.termFactory = termFactory;
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

        // TODO: generalize
        Optional<RDFAtomPredicate> triplePredicate = mappings.stream()
                .flatMap(m -> m.getRDFAtomPredicates().stream())
                .filter(p -> p instanceof TriplePredicate)
                .findAny();

        return triplePredicate
                .map(p -> {
                    ImmutableMap<IRI, IQ> propertyMap = mergeMappingPropertyMaps(mappings, p);
                    ImmutableMap<IRI, IQ> classMap = mergeMappingClassMaps(mappings, p);

                    // TODO: check that the ExecutorRegistry is identical for all mappings ?
                    return specificationFactory.createMapping(
                            metadata,
                            propertyMap, classMap
                    );
                })
                .orElseGet(() -> specificationFactory.createMapping(metadata,
                        ImmutableMap.of(), ImmutableMap.of()));
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
                        .map(m -> m.getMetadata().getUriTemplateMatcher()),
                termFactory
        );
    }

    private ImmutableMap<IRI, IQ> mergeMappingPropertyMaps(ImmutableSet<Mapping> mappings,
                                                           RDFAtomPredicate rdfAtomPredicate) {

        ImmutableMap<IRI, Collection<IQ>> atomPredicate2IQs = mappings.stream()
                .flatMap(m -> getMappingPropertyMap(m, rdfAtomPredicate).entrySet().stream())
                .collect(ImmutableCollectors.toMultimap())
                .asMap();

        return atomPredicate2IQs.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> mergeDefinitions(e.getValue())
                ));
    }

    private ImmutableMap<IRI, IQ> mergeMappingClassMaps(ImmutableSet<Mapping> mappings,
                                                        RDFAtomPredicate rdfAtomPredicate) {

        ImmutableMap<IRI, Collection<IQ>> atomPredicate2IQs = mappings.stream()
                .flatMap(m -> getMappingClassMap(m, rdfAtomPredicate).entrySet().stream())
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
    private IQ mergeDefinitions(Collection<IQ> queries) {
        return queryMerger.mergeDefinitions(queries)
                .map(mappingIQNormalizer::normalize)
                .orElseThrow(() -> new MappingMergingException("The query should be present"));
    }

    private ImmutableMap<IRI, IQ> getMappingPropertyMap(Mapping mapping, RDFAtomPredicate rdfAtomPredicate) {
        return mapping.getRDFProperties(rdfAtomPredicate).stream()
                .collect(ImmutableCollectors.toMap(
                        p -> p,
                        p -> getDefinition(mapping, p, rdfAtomPredicate)
                ));
    }

    private ImmutableMap<IRI, IQ> getMappingClassMap(Mapping mapping, RDFAtomPredicate rdfAtomPredicate) {
        return mapping.getRDFClasses(rdfAtomPredicate).stream()
                .collect(ImmutableCollectors.toMap(
                        p -> p,
                        p -> getDefinition(mapping, p, rdfAtomPredicate)
                ));
    }

    /**
     * Due to a Java compiler bug (hiding .orElseThrow() in a sub-method does the trick)
     */
    private static IQ getDefinition(Mapping mapping, IRI iri, RDFAtomPredicate rdfAtomPredicate) {
        return mapping.getRDFPropertyDefinition(rdfAtomPredicate, iri)
                .orElseGet(() -> mapping.getRDFClassDefinition(rdfAtomPredicate, iri)
                        .orElseThrow(() -> new MappingMergingException("This atom predicate should have a definition")));

    }
}
