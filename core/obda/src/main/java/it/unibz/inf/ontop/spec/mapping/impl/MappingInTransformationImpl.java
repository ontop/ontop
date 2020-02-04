package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MappingInTransformationImpl implements MappingInTransformation  {

    private final ImmutableSet<RDFAtomPredicate> rdfAtomPredicates;
    private final ImmutableMap<MappingAssertionIndex, IQ> assertions;

    @AssistedInject
    private MappingInTransformationImpl(
                        @Assisted ImmutableMap<MappingAssertionIndex, IQ> assertions,
                        OntopModelSettings settings) {

        this.assertions = assertions;

        if (settings.isTestModeEnabled()) {
            for (IQ query : assertions.values()) {
                VariableNullability variableNullability = query.getTree().getVariableNullability();
                if (!variableNullability.getNullableGroups().isEmpty())
                    throw new NullableVariableInMappingException(query, variableNullability.getNullableGroups());
            }
        }

        rdfAtomPredicates = assertions.keySet().stream()
                .map(MappingAssertionIndex::getPredicate)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public Optional<IQ> getAssertion(MappingAssertionIndex idx) {
        return Optional.ofNullable(assertions.get(idx));
    }

    @Override
    public ImmutableMap<MappingAssertionIndex, IQ> getAssertions() {
        return assertions;
    }

}
