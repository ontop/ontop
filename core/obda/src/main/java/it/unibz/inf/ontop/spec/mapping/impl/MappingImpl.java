package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.stream.Stream;


public class MappingImpl implements Mapping {

    private final MappingMetadata metadata;
    private final ImmutableMap<IRI, IntermediateQuery> propertyDefinitions;
    private final ImmutableMap<IRI, IntermediateQuery> classDefinitions;
    /**
     * TODO: remove it when the conversion to Datalog will not be needed anymore
     */
    private final ExecutorRegistry executorRegistry;

    @AssistedInject
    private MappingImpl(@Assisted MappingMetadata metadata,
                        @Assisted("propertyMap") ImmutableMap<IRI, IntermediateQuery> propertyMap,
                        @Assisted("classMap") ImmutableMap<IRI, IntermediateQuery> classMap,
                        @Assisted ExecutorRegistry executorRegistry,
                        OntopModelSettings settings) {
        this.metadata = metadata;
        this.propertyDefinitions = propertyMap;
        this.classDefinitions = classMap;
        this.executorRegistry = executorRegistry;

        if (settings.isTestModeEnabled()) {
            for (IntermediateQuery query : propertyDefinitions.values()) {
                if (projectNullableVariable(query))
                    throw new IllegalArgumentException(
                            "A mapping assertion must not return a nullable variable. \n" + query);
            }
            for (IntermediateQuery query : classDefinitions.values()) {
                if (projectNullableVariable(query))
                    throw new IllegalArgumentException(
                            "A mapping assertion must not return a nullable variable. \n" + query);
            }
        }

    }

    private static boolean projectNullableVariable(IntermediateQuery query) {
        QueryNode rootNode = query.getRootNode();
        return query.getProjectionAtom().getVariableStream()
                .anyMatch(v -> rootNode.isVariableNullable(query, v));
    }

    @Override
    public MappingMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Optional<IntermediateQuery> getRDFPropertyDefinition(IRI propertyIRI) {
        IntermediateQuery query = propertyDefinitions.get(propertyIRI);
        return query != null ?
                Optional.of(query):
                Optional.empty();
    }

    @Override
    public Optional<IntermediateQuery> getRDFClassDefinition(IRI classIRI) {
        IntermediateQuery query = classDefinitions.get(classIRI);
        return query != null ?
                Optional.of(query):
                Optional.empty();
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
    public ImmutableCollection<IntermediateQuery> getQueries() {
        return Stream.concat(classDefinitions.values().stream(), propertyDefinitions.values().stream())
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ExecutorRegistry getExecutorRegistry() {
        return executorRegistry;
    }
}
