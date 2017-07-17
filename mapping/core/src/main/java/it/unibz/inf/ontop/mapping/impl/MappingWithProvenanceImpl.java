package it.unibz.inf.ontop.mapping.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.transform.QueryMerger;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.pp.PPTriplesMapProvenance;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public class MappingWithProvenanceImpl implements MappingWithProvenance {

    private final ImmutableMap<IntermediateQuery, PPTriplesMapProvenance> provenanceMap;
    private final MappingMetadata mappingMetadata;
    private final ExecutorRegistry executorRegistry;
    private final SpecificationFactory specFactory;
    private final QueryMerger queryMerger;

    @AssistedInject
    private MappingWithProvenanceImpl(@Assisted ImmutableMap<IntermediateQuery, PPTriplesMapProvenance> provenanceMap,
                                      @Assisted MappingMetadata mappingMetadata,
                                      @Assisted ExecutorRegistry executorRegistry,
                                      SpecificationFactory specFactory,
                                      QueryMerger queryMerger) {
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
    public ImmutableMap<IntermediateQuery, PPTriplesMapProvenance> getProvenanceMap() {
        return provenanceMap;
    }

    @Override
    public PPTriplesMapProvenance getProvenance(IntermediateQuery mappingAssertion) {
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
    public MappingWithProvenance newMappingWithProvenance(
            ImmutableMap<IntermediateQuery, PPTriplesMapProvenance> newProvenanceMap) {
        return new MappingWithProvenanceImpl(newProvenanceMap, mappingMetadata, executorRegistry, specFactory, queryMerger);
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
