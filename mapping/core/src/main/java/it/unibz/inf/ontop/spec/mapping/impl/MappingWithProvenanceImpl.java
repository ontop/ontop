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
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Map;

public class MappingWithProvenanceImpl implements MappingWithProvenance {

    private final ImmutableList<MappingAssertion> assertions;
    private final SpecificationFactory specFactory;
    private final UnionBasedQueryMerger queryMerger;

    @AssistedInject
    private MappingWithProvenanceImpl(@Assisted ImmutableList<MappingAssertion> assertions,
                                      SpecificationFactory specFactory,
                                      UnionBasedQueryMerger queryMerger,
                                      OntopModelSettings settings) {
        this.assertions = assertions;
        this.specFactory = specFactory;
        this.queryMerger = queryMerger;

        if (settings.isTestModeEnabled()) {
            for (MappingAssertion a : assertions) {
                checkNullableVariables(a.getQuery());
            }
        }
    }

    private static void checkNullableVariables(IQ query) throws NullableVariableInMappingException {
        VariableNullability variableNullability = query.getTree().getVariableNullability();
        if (!variableNullability.getNullableGroups().isEmpty())
            throw new NullableVariableInMappingException(query, variableNullability.getNullableGroups());
    }

    @Override
    public ImmutableList<MappingAssertion> getMappingAssertions() {
        return assertions;
    }

    @Override
    public MappingInTransformation toRegularMapping() {

        ImmutableMap<MappingAssertionIndex, IQ> iqClassificationMap = assertions.stream()
                .collect(ImmutableCollectors.toMultimap(a -> a.getIndex(), a -> a.getQuery()))
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
