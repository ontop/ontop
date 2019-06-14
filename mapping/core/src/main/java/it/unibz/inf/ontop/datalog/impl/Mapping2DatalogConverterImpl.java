package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.exception.UnsupportedFeatureForDatalogConversionException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.stream.Stream;

@Singleton
public class Mapping2DatalogConverterImpl implements Mapping2DatalogConverter {

    private final QueryUnionSplitter unionSplitter;
    private final IQ2DatalogTranslator iq2DatalogTranslator;
    private final UnionFlattener unionNormalizer;

    // For the translation of subqueries: prevents conflicts in generated predicate names

    @Inject
    private Mapping2DatalogConverterImpl(QueryUnionSplitter unionSplitter,
                                         IQ2DatalogTranslator iq2DatalogTranslator,
                                         UnionFlattener unionNormalizer) {
        this.unionSplitter = unionSplitter;
        this.iq2DatalogTranslator = iq2DatalogTranslator;
        this.unionNormalizer = unionNormalizer;
    }

    @Override
    public Stream<CQIE> convert(Mapping mapping) {
        return mapping.getRDFAtomPredicates().stream()
                .flatMap(p -> mapping.getQueries(p).stream())
                .flatMap(this::convertMappingQuery);
    }

    @Override
    public ImmutableMap<CQIE, PPMappingAssertionProvenance> convert(MappingWithProvenance mappingWithProvenance) {
        return mappingWithProvenance.getProvenanceMap().entrySet().stream()
                .flatMap(e -> convertMappingQuery(e.getKey())
                        .map(r -> new AbstractMap.SimpleEntry<>(r, e.getValue())))
                .collect(ImmutableCollectors.toMap());
    }

    private Stream<CQIE> convertMappingQuery(IQ mappingQuery) {
        return unionSplitter.splitUnion(unionNormalizer.optimize(mappingQuery))
                .flatMap(q -> {
                    try {
                        return iq2DatalogTranslator.translate(q).getRules().stream();
                    } catch (UnsupportedFeatureForDatalogConversionException e) {
                        throw new RuntimeException("Unsupported feature in mapping source query: "+e.getMessage());
                    }
                })
                .collect(ImmutableCollectors.toSet())
                .stream();
    }
}
