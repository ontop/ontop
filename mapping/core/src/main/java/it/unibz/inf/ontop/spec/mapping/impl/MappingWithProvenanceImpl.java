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
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class MappingWithProvenanceImpl implements MappingWithProvenance {

    private final ImmutableMap<IQ, PPMappingAssertionProvenance> provenanceMap;
    private final SpecificationFactory specFactory;
    private final UnionBasedQueryMerger queryMerger;

    @AssistedInject
    private MappingWithProvenanceImpl(@Assisted ImmutableMap<IQ, PPMappingAssertionProvenance> provenanceMap,
                                      SpecificationFactory specFactory,
                                      UnionBasedQueryMerger queryMerger,
                                      OntopModelSettings settings) {
        this.provenanceMap = provenanceMap;
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
    public MappingInTransformation toRegularMapping() {

        ImmutableMap<MappingAssertionIndex, IQ> iqClassificationMap = getMappingAssertions().stream()
                .collect(ImmutableCollectors.toMultimap(MappingTools::extractRDFPredicate, iq -> iq))
                .asMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> mergeDefinitions(e.getValue())));

        return specFactory.createMapping(iqClassificationMap);
    }

    private IQ mergeDefinitions(Collection<IQ> assertions) {
        return queryMerger.mergeDefinitions(assertions)
                .map(IQ::normalizeForOptimization)
                .orElseThrow(() -> new MinorOntopInternalBugException("Could not merge assertions: " + assertions));
    }
}
