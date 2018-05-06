package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
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
    private final SpecificationFactory specFactory;
    private final UnionBasedQueryMerger queryMerger;

    @AssistedInject
    private MappingWithProvenanceImpl(@Assisted ImmutableMap<IQ, PPMappingAssertionProvenance> provenanceMap,
                                      @Assisted MappingMetadata mappingMetadata,
                                      SpecificationFactory specFactory,
                                      UnionBasedQueryMerger queryMerger,
                                      OntopModelSettings settings) {
        this.provenanceMap = provenanceMap;
        this.mappingMetadata = mappingMetadata;
        this.specFactory = specFactory;
        this.queryMerger = queryMerger;

        if (settings.isTestModeEnabled()) {
            for (IQ query : provenanceMap.keySet()) {
                checkNullableVariables(query);
            }
        }
    }

    private static void checkNullableVariables(IQ query) throws NullableVariableInMappingException {
        ImmutableSet<Variable> nullableVariables = query.getTree().getNullableVariables();
        if (!nullableVariables.isEmpty())
            throw new NullableVariableInMappingException(query, nullableVariables);
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
                .map(IQ::normalizeForOptimization)
                .collect(ImmutableCollectors.toMap(
                        MappingTools::extractClassIRI,
                        a -> a));

        ImmutableMap<IRI, IQ> propertyDefinitionMap = propertyMultimap.asMap().values().stream()
                .map(queryMerger::mergeDefinitions)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(IQ::normalizeForOptimization)
                .collect(ImmutableCollectors.toMap(
                        MappingTools::extractPropertiesIRI,
                        a -> a));

        return specFactory.createMapping(mappingMetadata, propertyDefinitionMap, classDefinitionMap);

    }

    @Override
    public MappingMetadata getMetadata() {
        return mappingMetadata;
    }

}
