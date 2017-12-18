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
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;

import java.util.Optional;


public class MappingImpl implements Mapping {

    private final MappingMetadata metadata;
    private final ImmutableMap<AtomPredicate, IntermediateQuery> definitions;
    /**
     * TODO: remove it when the conversion to Datalog will not be needed anymore
     */
    private final ExecutorRegistry executorRegistry;

    @AssistedInject
    private MappingImpl(@Assisted MappingMetadata metadata,
                        @Assisted ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap,
                        @Assisted ExecutorRegistry executorRegistry,
                        OntopModelSettings settings) {
        this.metadata = metadata;
        this.definitions = mappingMap;
        this.executorRegistry = executorRegistry;

        if (settings.isTestModeEnabled()) {
            for (IntermediateQuery query : mappingMap.values()) {
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
    public Optional<IntermediateQuery> getDefinition(AtomPredicate predicate) {
        IntermediateQuery query = definitions.get(predicate);
        return query != null && query.getProjectionAtom().getPredicate().getArity() == predicate.getArity()?
            Optional.of(query):
            Optional.empty();
    }

    @Override
    public ImmutableSet<AtomPredicate> getPredicates() {
        return definitions.keySet();
    }

    @Override
    public ImmutableCollection<IntermediateQuery> getQueries() {
        return definitions.values();
    }

    @Override
    public ExecutorRegistry getExecutorRegistry() {
        return executorRegistry;
    }
}
