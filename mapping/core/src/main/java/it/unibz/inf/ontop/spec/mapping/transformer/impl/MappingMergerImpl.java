package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingMergingException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingMerger;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

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

        ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyTable = mergeMappingTables(mappings, MappingInTransformation::getRDFPropertyQueries, false);
        ImmutableTable<RDFAtomPredicate, IRI, IQ> classTable = mergeMappingTables(mappings, MappingInTransformation::getRDFClassQueries, true);

        return specificationFactory.createMapping(Stream.concat(
                propertyTable.cellSet().stream()
                        .map(e ->
                                Maps.immutableEntry(
                                        new MappingAssertionIndex(e.getRowKey(), e.getColumnKey(), false),
                                        e.getValue())),
                classTable.cellSet().stream()
                        .map(e ->
                                Maps.immutableEntry(
                                        new MappingAssertionIndex(e.getRowKey(), e.getColumnKey(), true),
                                        e.getValue())))
                .collect(ImmutableCollectors.toMap()));
    }

    private ImmutableTable<RDFAtomPredicate, IRI, IQ> mergeMappingTables(ImmutableSet<MappingInTransformation> mappings, Function<MappingInTransformation, ImmutableSet<Table.Cell<RDFAtomPredicate, IRI, IQ>>> extractor, boolean isClass) {

        ImmutableMap<MappingAssertionIndex, Collection<IQ>> multiTable = mappings.stream()
                .flatMap(m -> extractor.apply(m).stream()
                        .map(c -> Maps.immutableEntry(
                                new MappingAssertionIndex(c.getRowKey(), c.getColumnKey(), isClass),
                                c.getValue())))
                .collect(ImmutableCollectors.toMultimap())
                .asMap();

        return multiTable.entrySet().stream()
                .map(e -> Tables.immutableCell(
                        e.getKey().getPredicate(),
                        e.getKey().getIri(),
                        queryMerger.mergeDefinitions(e.getValue())
                                .orElseThrow(() -> new MappingMergingException("The query should be present"))))
                .collect(ImmutableCollectors.toTable());
    }

}
