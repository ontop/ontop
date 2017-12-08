package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.QuadrupleDefinition;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;

import java.util.Optional;

public class TemporalMappingImpl implements TemporalMapping{

    private final MappingMetadata metadata;
    private final ImmutableMap<AtomPredicate, QuadrupleDefinition> definitions;
    /**
     * TODO: remove it when the conversion to Datalog will not be needed anymore
     */
    private final ExecutorRegistry executorRegistry;

    @AssistedInject
    private TemporalMappingImpl(@Assisted MappingMetadata metadata,
                        @Assisted ImmutableMap<AtomPredicate, QuadrupleDefinition> temporalMappingMap,
                        @Assisted ExecutorRegistry executorRegistry,
                        OntopModelSettings settings) {
        this.metadata = metadata;
        this.definitions = temporalMappingMap;
        this.executorRegistry = executorRegistry;

        if (settings.isTestModeEnabled()) {

            temporalMappingMap.values().forEach(qd -> {
                        for (IntermediateQuery query : qd.getAll()) {
                            if (projectNullableVariable(query))
                                throw new IllegalArgumentException(
                                        "A mapping assertion must not return a nullable variable. \n" + query);
                        }
                    }
            );
        }
    }

    private static boolean projectNullableVariable(IntermediateQuery query) {
        ConstructionNode rootNode = query.getRootConstructionNode();
        return rootNode.getVariables().stream()
                .anyMatch(v -> rootNode.isVariableNullable(query, v));
    }


    public MappingMetadata getMetadata() {
        return metadata;
    }


//    public Optional<IntermediateQuery> getDefinition(AtomPredicate predicate1, AtomPredicate predicate2) {
//        return Optional.ofNullable(definitions.get(predicate1));
//    }


    public ImmutableSet<AtomPredicate> getPredicates() {
        return definitions.keySet();
    }

    public ImmutableMap<AtomPredicate, QuadrupleDefinition> getDefinitions() {
        return definitions;

    }

    public ExecutorRegistry getExecutorRegistry() {
        return executorRegistry;
    }
}
