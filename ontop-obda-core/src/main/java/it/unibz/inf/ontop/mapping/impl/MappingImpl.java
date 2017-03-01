package it.unibz.inf.ontop.mapping.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

import java.util.Optional;


public class MappingImpl implements Mapping {

    private final MappingMetadata metadata;
    private final ImmutableMap<AtomPredicate, IntermediateQuery> definitions;

    @AssistedInject
    private MappingImpl(@Assisted MappingMetadata metadata,
                        @Assisted ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap) {
        this.metadata = metadata;
        this.definitions = mappingMap;
    }

    @Override
    public MappingMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Optional<IntermediateQuery> getDefinition(AtomPredicate predicate) {
        return Optional.ofNullable(definitions.get(predicate));
    }

    @Override
    public ImmutableSet<AtomPredicate> getPredicates() {
        return definitions.keySet();
    }

}
