package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.IntermediateQuery2DatalogTranslator;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.QueryUnionSplitter;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.datalog.DatalogNormalizer;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.stream.Stream;

@Singleton
public class Mapping2DatalogConverterImpl implements Mapping2DatalogConverter {

    private final QueryUnionSplitter unionSplitter;
    private final IntermediateQuery2DatalogTranslator iq2DatalogTranslator;
    private final DatalogNormalizer datalogNormalizer;

    // For the translation of subqueries: prevents conflicts in generated predicate names

    @Inject
    private Mapping2DatalogConverterImpl(QueryUnionSplitter unionSplitter,
                                         IntermediateQuery2DatalogTranslator iq2DatalogTranslator,
                                         DatalogNormalizer datalogNormalizer) {
        this.unionSplitter = unionSplitter;
        this.iq2DatalogTranslator = iq2DatalogTranslator;
        this.datalogNormalizer = datalogNormalizer;
    }

    @Override
    public Stream<CQIE> convert(Mapping mapping) {
        return mapping.getQueries().stream()
                .flatMap(this::convertMappingQuery);
    }

    @Override
    public ImmutableMap<CQIE, PPMappingAssertionProvenance> convert(MappingWithProvenance mappingWithProvenance) {
        return mappingWithProvenance.getProvenanceMap().entrySet().stream()
                .flatMap(e -> convertMappingQuery(e.getKey())
                        .map(r -> new AbstractMap.SimpleEntry<>(r, e.getValue())))
                .collect(ImmutableCollectors.toMap());
    }

    private Stream<CQIE> convertMappingQuery(IntermediateQuery mappingQuery) {


        ImmutableSet<CQIE> rules = unionSplitter.splitUnion(mappingQuery)
                .flatMap(q -> iq2DatalogTranslator.translate(q).getRules().stream())
                .collect(ImmutableCollectors.toSet());
        //CQIEs are mutable
        rules.forEach(datalogNormalizer::unfoldJoinTrees);
        return rules.stream();
    }
}
