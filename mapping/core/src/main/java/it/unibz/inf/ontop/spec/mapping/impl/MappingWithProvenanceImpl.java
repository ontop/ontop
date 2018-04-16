package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.MappingIQNormalizer;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public class MappingWithProvenanceImpl implements MappingWithProvenance {

    private final ImmutableMap<IQ, PPMappingAssertionProvenance> provenanceMap;
    private final MappingMetadata mappingMetadata;
    private final ExecutorRegistry executorRegistry;
    private final SpecificationFactory specFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final MappingIQNormalizer mappingIQNormalizer;

    @AssistedInject
    private MappingWithProvenanceImpl(@Assisted ImmutableMap<IQ, PPMappingAssertionProvenance> provenanceMap,
                                      @Assisted MappingMetadata mappingMetadata,
                                      @Assisted ExecutorRegistry executorRegistry,
                                      SpecificationFactory specFactory,
                                      UnionBasedQueryMerger queryMerger,
                                      MappingIQNormalizer mappingIQNormalizer) {
        this.provenanceMap = provenanceMap;
        this.mappingMetadata = mappingMetadata;
        this.executorRegistry = executorRegistry;
        this.specFactory = specFactory;
        this.queryMerger = queryMerger;
        this.mappingIQNormalizer = mappingIQNormalizer;
    }

    @Override
    public ImmutableSet<IQ> getMappingAssertions() {
        return provenanceMap.keySet();
    }

    @Override
    public ImmutableMap<IQ, PPMappingAssertionProvenance> getProvenanceMap() {
        return provenanceMap;
    }

    @Override
    public PPMappingAssertionProvenance getProvenance(IQ mappingAssertion) {
        return Optional.ofNullable(provenanceMap.get(mappingAssertion))
                .orElseThrow(() -> new IllegalArgumentException("This assertion is not part of the mapping"));
    }

    @Override
    public Mapping toRegularMapping() {

        // return iri of class in multimap
        ImmutableMultimap<IRI, IQ> classMultimap = getMappingAssertions().stream()
                .filter (assertion ->  {
                    ImmutableList<Variable> projectedVariables = assertion.getProjectionAtom().getArguments();
                    IRI predicateIRI =  MappingTools.extractPredicateTerm(assertion, projectedVariables.get(1));
                        return (predicateIRI.equals(RDF.TYPE));})
                .collect(ImmutableCollectors.toMultimap(
                        a -> {
                            ImmutableList<Variable> projectedVariables = a.getProjectionAtom().getArguments();
                                return MappingTools.extractPredicateTerm(a, projectedVariables.get(2));
                            },
                        a -> a));

        // return iri of object and data properties in multimap
        ImmutableMultimap<IRI, IQ> propertyMultimap = getMappingAssertions().stream()
                .filter (assertion ->  {
                    ImmutableList<Variable> projectedVariables = assertion.getProjectionAtom().getArguments();
                    IRI predicateIRI =  MappingTools.extractPredicateTerm(assertion, projectedVariables.get(1));
                    return (!predicateIRI.equals(RDF.TYPE));})
                .collect(ImmutableCollectors.toMultimap(
                        a -> {
                            ImmutableList<Variable> projectedVariables = a.getProjectionAtom().getArguments();
                            return MappingTools.extractPredicateTerm(a, projectedVariables.get(1));
                        },
                        a -> a));


        ImmutableMap<IRI, IQ> classDefinitionMap = classMultimap.asMap().values().stream()
                .map(queryMerger::mergeDefinitions)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(mappingIQNormalizer::normalize)
                .collect(ImmutableCollectors.toMap(
                        MappingTools::extractClassIRI,
                        a -> a));

        ImmutableMap<IRI, IQ> propertyDefinitionMap = propertyMultimap.asMap().values().stream()
                .map(queryMerger::mergeDefinitions)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(mappingIQNormalizer::normalize)
                .collect(ImmutableCollectors.toMap(
                        MappingTools::extractPropertiesIRI,
                        a -> a));

        return specFactory.createMapping(mappingMetadata, propertyDefinitionMap, classDefinitionMap, executorRegistry);

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
