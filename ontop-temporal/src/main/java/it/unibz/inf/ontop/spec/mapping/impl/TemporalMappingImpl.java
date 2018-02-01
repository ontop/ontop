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
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Collectors;

public class TemporalMappingImpl implements TemporalMapping {

    private final MappingMetadata metadata;
    private final ImmutableMap<AtomPredicate, IntervalAndIntermediateQuery> definitions;
    /**
     * TODO: remove it when the conversion to Datalog will not be needed anymore
     */
    private final ExecutorRegistry executorRegistry;

    @AssistedInject
    private TemporalMappingImpl(@Assisted MappingMetadata metadata,
                        @Assisted ImmutableMap<AtomPredicate, IntervalAndIntermediateQuery> mappingMap,
                        @Assisted ExecutorRegistry executorRegistry,
                        OntopModelSettings settings) {
        this.metadata = metadata;
        this.definitions = mappingMap;
        this.executorRegistry = executorRegistry;

        if (settings.isTestModeEnabled()) {
            for (IntervalAndIntermediateQuery query : mappingMap.values()) {
                if (projectNullableVariable(query.getIntermediateQuery()))
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
        IntermediateQuery query = definitions.get(predicate).getIntermediateQuery();
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
        return definitions.values().stream().map(IntervalAndIntermediateQuery::getIntermediateQuery).collect(ImmutableCollectors.toList());
    }

    @Override
    public ExecutorRegistry getExecutorRegistry() {
        return executorRegistry;
    }

    @Override
    public IntervalAndIntermediateQuery getIntervalAndIntermediateQuery(AtomPredicate predicate) {
        return definitions.get(predicate);
    }

    @Override
    public ImmutableMap<AtomPredicate, IntervalAndIntermediateQuery> getDefinitions() {
        return definitions;
    }
}
