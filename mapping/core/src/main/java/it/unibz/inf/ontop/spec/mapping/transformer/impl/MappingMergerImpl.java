package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingMergingException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingMerger;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Map;

public class MappingMergerImpl implements MappingMerger {

    private final SpecificationFactory specificationFactory;
    private final UnionBasedQueryMerger queryMerger;

    @Inject
    private MappingMergerImpl(SpecificationFactory specificationFactory, UnionBasedQueryMerger queryMerger) {
        this.specificationFactory = specificationFactory;
        this.queryMerger = queryMerger;
    }

    @Override
    public MappingInTransformation merge(MappingInTransformation ... mappings) {
       return merge(ImmutableSet.copyOf(mappings));
    }

    @Override
    public MappingInTransformation merge(ImmutableSet<MappingInTransformation> mappings) {

        if (mappings.isEmpty()) {
            throw new IllegalArgumentException("The set of mappings is assumed to be nonempty");
        }

        ImmutableMap<MappingAssertionIndex, Collection<IQ>> multiTable = mappings.stream()
                .flatMap(m -> m.getAssertions().entrySet().stream())
                .collect(ImmutableCollectors.toMultimap())
                .asMap();

        return specificationFactory.createMapping(multiTable.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> queryMerger.mergeDefinitions(e.getValue())
                                .orElseThrow(() -> new MappingMergingException("The query should be present")))));
    }
}
