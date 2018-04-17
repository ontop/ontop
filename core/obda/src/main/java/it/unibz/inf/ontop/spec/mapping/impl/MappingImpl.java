package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.stream.Stream;


public class MappingImpl implements Mapping {

    private final MappingMetadata metadata;
    private final ImmutableMap<IRI, IQ> propertyDefinitions;
    private final ImmutableMap<IRI, IQ> classDefinitions;

    @AssistedInject
    private MappingImpl(@Assisted MappingMetadata metadata,
                        @Assisted("propertyMap") ImmutableMap<IRI, IQ> propertyMap,
                        @Assisted("classMap") ImmutableMap<IRI, IQ> classMap,
                        OntopModelSettings settings) {
        this.metadata = metadata;
        this.propertyDefinitions = propertyMap;
        this.classDefinitions = classMap;

        if (settings.isTestModeEnabled()) {
            for (IQ query : propertyDefinitions.values()) {
                checkNullableVariables(query);
            }
            for (IQ query : classDefinitions.values()) {
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
    public MappingMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Optional<IQ> getRDFPropertyDefinition(IRI propertyIRI) {
        return Optional.ofNullable(propertyDefinitions.get(propertyIRI));
    }

    @Override
    public Optional<IQ> getRDFClassDefinition(IRI classIRI) {
        return Optional.ofNullable(classDefinitions.get(classIRI));
    }



    @Override
    public ImmutableSet<IRI> getRDFProperties() {
        return propertyDefinitions.keySet();
    }

    @Override
    public ImmutableSet<IRI> getRDFClasses() {
        return classDefinitions.keySet();
    }

    @Override
    public ImmutableCollection<IQ> getQueries() {
        return Stream.concat(classDefinitions.values().stream(), propertyDefinitions.values().stream())
                .collect(ImmutableCollectors.toList());
    }

    private static class NullableVariableInMappingException extends OntopInternalBugException {
        private NullableVariableInMappingException(IQ definition, ImmutableSet<Variable> nullableVariables) {
            super("The following definition projects nullable variables: " + nullableVariables
                    + ".\n Definition:\n" + definition);
        }
    }
}
