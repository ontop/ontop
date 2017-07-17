package it.unibz.inf.ontop.mapping.datalog.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.QueryUnionSplitter;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.inf.ontop.owlrefplatform.core.translator.IntermediateQueryToDatalogTranslator;
import it.unibz.inf.ontop.pp.PPTriplesMapProvenance;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.stream.Stream;

@Singleton
public class Mapping2DatalogConverterImpl implements Mapping2DatalogConverter {

    private final QueryUnionSplitter unionSplitter;
    // For the translation of subqueries: prevents conflicts in generated predicate names
    private int ruleCounter;

    @Inject
    private Mapping2DatalogConverterImpl(QueryUnionSplitter unionSplitter) {
        this.unionSplitter = unionSplitter;
    }

    @Override
    public Stream<CQIE> convert(Mapping mapping) {
        return mapping.getQueries().stream()
                .flatMap(this::convertMappingQuery);
    }

    @Override
    public ImmutableMap<CQIE, PPTriplesMapProvenance> convert(MappingWithProvenance mappingWithProvenance) {
        return mappingWithProvenance.getProvenanceMap().entrySet().stream()
                .flatMap(e -> convertMappingQuery(e.getKey())
                        .map(r -> new AbstractMap.SimpleEntry<>(r, e.getValue())))
                .collect(ImmutableCollectors.toMap());
    }

    private Stream<CQIE> convertMappingQuery(IntermediateQuery mappingQuery) {
        ImmutableSet<CQIE> rules = unionSplitter.splitUnion(mappingQuery)
                .flatMap(q -> IntermediateQueryToDatalogTranslator.translate(q, ruleCounter).getRules().stream())
                .collect(ImmutableCollectors.toSet());
        //CQIEs are mutable
        rules.forEach(DatalogNormalizer::unfoldJoinTrees);
        ruleCounter+=rules.size();
        return rules.stream();
    }
}
