package it.unibz.inf.ontop.mapping.datalog.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.owlrefplatform.core.translator.IntermediateQueryToDatalogTranslator;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.QueryUnionSplitter;

import java.util.stream.Stream;

@Singleton
public class Mapping2DatalogConverterImpl implements Mapping2DatalogConverter {

    private final QueryUnionSplitter unionSplitter;

    @Inject
    private Mapping2DatalogConverterImpl(QueryUnionSplitter unionSplitter) {
        this.unionSplitter = unionSplitter;
    }

    @Override
    public Stream<CQIE> convert(Mapping mapping) {
        return mapping.getQueries().stream()
                .flatMap(this::convertMappingQuery);
    }

    private Stream<CQIE> convertMappingQuery(IntermediateQuery mappingQuery) {
        return unionSplitter.splitUnion(mappingQuery)
                .flatMap(q -> IntermediateQueryToDatalogTranslator.translate(q).getRules().stream());
    }
}
