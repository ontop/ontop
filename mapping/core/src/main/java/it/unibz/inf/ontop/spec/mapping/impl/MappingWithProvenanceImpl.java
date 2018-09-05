package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Collection;
import java.util.Map;
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
        VariableNullability variableNullability = query.getTree().getVariableNullability();
        if (!variableNullability.getNullableGroups().isEmpty())
            throw new NullableVariableInMappingException(query, variableNullability.getNullableGroups());
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

        ImmutableMap<IQ, MappingTools.RDFPredicateInfo> iqClassificationMap = getMappingAssertions().stream()
                .collect(ImmutableCollectors.toMap(
                        iq -> iq,
                        MappingTools::extractRDFPredicate
                ));

        return specFactory.createMapping(mappingMetadata,
                    extractTable(iqClassificationMap, false),
                    extractTable(iqClassificationMap, true));
    }

    private ImmutableTable<RDFAtomPredicate, IRI, IQ> extractTable(
            ImmutableMap<IQ, MappingTools.RDFPredicateInfo> iqClassificationMap, boolean isClass) {

        ImmutableMultimap<Map.Entry<RDFAtomPredicate, IRI>, IQ> multimap = iqClassificationMap.entrySet().stream()
                .filter(e -> e.getValue().isClass() == isClass)
                .collect(ImmutableCollectors.toMultimap(
                        e -> Maps.immutableEntry(
                                (RDFAtomPredicate) e.getKey().getProjectionAtom().getPredicate(),
                                e.getValue().getIri()),
                        Map.Entry::getKey));

        return multimap.asMap().entrySet().stream()
                .map(e -> Tables.immutableCell(
                        e.getKey().getKey(),
                        e.getKey().getValue(),
                        mergeDefinitions(e.getValue())))
                .collect(ImmutableCollectors.toTable());
    }

    private IQ mergeDefinitions(Collection<IQ> assertions) {
        return queryMerger.mergeDefinitions(assertions)
                .orElseThrow(() -> new MinorOntopInternalBugException("Could not merge assertions: " + assertions));
    }


    @Override
    public MappingMetadata getMetadata() {
        return mappingMetadata;
    }

}
