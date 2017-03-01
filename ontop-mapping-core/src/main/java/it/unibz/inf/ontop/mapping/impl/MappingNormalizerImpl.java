package it.unibz.inf.ontop.mapping.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingNormalizer;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryRenamer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

@Singleton
public class MappingNormalizerImpl implements MappingNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final SpecificationFactory specificationFactory;

    @Inject
    private MappingNormalizerImpl(IntermediateQueryFactory iqFactory, SpecificationFactory specificationFactory) {
        this.iqFactory = iqFactory;
        this.specificationFactory = specificationFactory;
    }

    @Override
    public Mapping normalize(Mapping mapping) {
        Stream<IntermediateQuery> queryStream = mapping.getPredicates().stream()
                .map(mapping::getDefinition)
                .filter(Optional::isPresent)
                .map(Optional::get);

        ImmutableMap<AtomPredicate, IntermediateQuery> normalizedMappingMap = renameQueries(queryStream)
                .collect(ImmutableCollectors.toMap(
                        q -> q.getProjectionAtom().getPredicate(),
                        q -> q));

        return specificationFactory.createMapping(mapping.getMetadata(), normalizedMappingMap,
                mapping.getExecutorRegistry());
    }

    /**
     * Appends a different suffix to each query
     */
    private Stream<IntermediateQuery> renameQueries(Stream<IntermediateQuery> queryStream) {
        AtomicInteger i = new AtomicInteger(0);
        return queryStream
                .map(m -> appendSuffixToVariableNames(iqFactory, m, i.incrementAndGet()));
    }

    private static IntermediateQuery appendSuffixToVariableNames(IntermediateQueryFactory iqFactory,
                                                                 IntermediateQuery query, int suffix) {
        Map<Variable, Variable> substitutionMap =
                query.getKnownVariables().stream()
                        .collect(Collectors.toMap(v -> v, v -> DATA_FACTORY.getVariable(v.getName()+"m"+suffix)));
        QueryRenamer queryRenamer = new QueryRenamer(iqFactory, DATA_FACTORY.getInjectiveVar2VarSubstitution(substitutionMap));
        return queryRenamer.transform(query);
    }
}
