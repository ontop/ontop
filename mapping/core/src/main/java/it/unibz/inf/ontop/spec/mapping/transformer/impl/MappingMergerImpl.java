package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingMergingException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingMerger;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
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

        ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyTable = mergeMappingPropertyTables(mappings);
        ImmutableTable<RDFAtomPredicate, IRI, IQ> classTable = mergeMappingClassTables(mappings);

        return specificationFactory.createMapping(propertyTable, classTable);
    }
/*
    private PrefixManager mergePrefixManagers(ImmutableSet<MappingInTransformation> mappings) {
        ImmutableMap<String, Collection<String>> prefixToUris = mappings.stream()
                .flatMap(m -> m.getPrefixManager().getPrefixMap().entrySet().stream())
                .collect(ImmutableCollectors.toMultimap())
                .asMap();

        ImmutableMap<String, String> prefixToUri = prefixToUris.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> flattenURIList(e.getKey(), e.getValue())
                ));
        return new SimplePrefixManager(prefixToUri);
    }

    private String flattenURIList(String prefix, Collection<String> uris) {
        if (ImmutableSet.copyOf(uris).size() == 1) {
            return uris.iterator().next();
        }
        throw new MappingMergingException("Conflicting URIs for prefix " + prefix + ": " + uris);
    }
*/
    private ImmutableTable<RDFAtomPredicate, IRI, IQ> mergeMappingPropertyTables(ImmutableSet<MappingInTransformation> mappings) {

        ImmutableMap<Map.Entry<RDFAtomPredicate, IRI>, Collection<IQ>> multiTable = mappings.stream()
                .flatMap(m -> extractCellStream(m,
                        p -> m.getRDFProperties(p).stream(),
                        (p, i) -> m.getRDFPropertyDefinition(p, i).get()))
                .collect(ImmutableCollectors.toMultimap(
                        c -> Maps.immutableEntry(c.getRowKey(), c.getColumnKey()),
                        Table.Cell::getValue))
                .asMap();

        return multiTable.entrySet().stream()
                .map(e -> Tables.immutableCell(
                        e.getKey().getKey(),
                        e.getKey().getValue(),
                        queryMerger.mergeDefinitions((e.getValue()))
                                .orElseThrow(() -> new MappingMergingException("The query should be present"))))
                .collect(ImmutableCollectors.toTable());
    }

    private Stream<Table.Cell<RDFAtomPredicate, IRI, IQ>> extractCellStream(
            MappingInTransformation m,
            Function<RDFAtomPredicate, Stream<IRI>> iriExtractor,
            BiFunction<RDFAtomPredicate, IRI, IQ> iqExtractor) {

        return m.getRDFAtomPredicates().stream()
                .flatMap(p -> iriExtractor.apply(p)
                            .map(i -> Tables.immutableCell(p, i, iqExtractor.apply(p, i))));
    }

    private ImmutableTable<RDFAtomPredicate, IRI, IQ> mergeMappingClassTables(ImmutableSet<MappingInTransformation> mappings) {

        ImmutableMap<Map.Entry<RDFAtomPredicate, IRI>, Collection<IQ>> multiTable = mappings.stream()
                .flatMap(m -> extractCellStream(m,
                        p -> m.getRDFClasses(p).stream(),
                        (p, i) -> m.getRDFClassDefinition(p, i).get()))
                .collect(ImmutableCollectors.toMultimap(
                        c -> Maps.immutableEntry(c.getRowKey(), c.getColumnKey()),
                        Table.Cell::getValue))
                .asMap();

        return multiTable.entrySet().stream()
                .map(e -> Tables.immutableCell(
                        e.getKey().getKey(),
                        e.getKey().getValue(),
                        queryMerger.mergeDefinitions((e.getValue()))
                                .orElseThrow(() -> new MappingMergingException("The query should be present"))))
                .collect(ImmutableCollectors.toTable());
    }
}
