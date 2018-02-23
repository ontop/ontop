package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.optimizer.MappingIQNormalizer;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public class MappingWithProvenanceImpl implements MappingWithProvenance {

    private final ImmutableMap<IntermediateQuery, PPMappingAssertionProvenance> provenanceMap;
    private final MappingMetadata mappingMetadata;
    private final ExecutorRegistry executorRegistry;
    private final SpecificationFactory specFactory;
    private final UnionBasedQueryMerger queryMerger;

    @AssistedInject
    private MappingWithProvenanceImpl(@Assisted ImmutableMap<IntermediateQuery, PPMappingAssertionProvenance> provenanceMap,
                                      @Assisted MappingMetadata mappingMetadata,
                                      @Assisted ExecutorRegistry executorRegistry,
                                      SpecificationFactory specFactory,
                                      UnionBasedQueryMerger queryMerger) {
        this.provenanceMap = provenanceMap;
        this.mappingMetadata = mappingMetadata;
        this.executorRegistry = executorRegistry;
        this.specFactory = specFactory;
        this.queryMerger = queryMerger;
    }

    @Override
    public ImmutableSet<IntermediateQuery> getMappingAssertions() {
        return provenanceMap.keySet();
    }

    @Override
    public ImmutableMap<IntermediateQuery, PPMappingAssertionProvenance> getProvenanceMap() {
        return provenanceMap;
    }

    @Override
    public PPMappingAssertionProvenance getProvenance(IntermediateQuery mappingAssertion) {
        return Optional.ofNullable(provenanceMap.get(mappingAssertion))
                .orElseThrow(() -> new IllegalArgumentException("This assertion is not part of the mapping"));
    }

    @Override
    public Mapping toRegularMapping() {
        ImmutableMultimap<AtomPredicate, IntermediateQuery> assertionMultimap = getMappingAssertions().stream()
                .collect(ImmutableCollectors.toMultimap(
                        q -> q.getProjectionAtom().getPredicate(),
                        q -> q));

        ImmutableMap<AtomPredicate, IntermediateQuery> definitionMap = assertionMultimap.asMap().values().stream()
                .map(queryMerger::mergeDefinitions)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toMap(
                        q -> q.getProjectionAtom().getPredicate(),
                        q -> q));

        return specFactory.createMapping(mappingMetadata, definitionMap, executorRegistry);

    }

    @Override
    public ExecutorRegistry getExecutorRegistry() {
        return executorRegistry;
    }

    @Override
    public MappingMetadata getMetadata() {
        return mappingMetadata;
    }

}
