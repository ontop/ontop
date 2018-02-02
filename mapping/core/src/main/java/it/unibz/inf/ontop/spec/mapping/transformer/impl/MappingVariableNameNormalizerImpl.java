package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingVariableNameNormalizer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Singleton
public class MappingVariableNameNormalizerImpl implements MappingVariableNameNormalizer {

    private final SpecificationFactory specificationFactory;
    private final QueryTransformerFactory transformerFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    private MappingVariableNameNormalizerImpl(SpecificationFactory specificationFactory,
                                              QueryTransformerFactory transformerFactory,
                                              SubstitutionFactory substitutionFactory,
                                              TermFactory termFactory) {
        this.specificationFactory = specificationFactory;
        this.transformerFactory = transformerFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
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
                        q -> q
                ));

        return specificationFactory.createMapping(mapping.getMetadata(), normalizedMappingMap,
                mapping.getExecutorRegistry());
    }

    /**
     * Appends a different suffix to each query
     */
    private Stream<IntermediateQuery> renameQueries(Stream<IntermediateQuery> queryStream) {
        AtomicInteger i = new AtomicInteger(0);
        return queryStream
                .map(m -> appendSuffixToVariableNames(transformerFactory, m, i.incrementAndGet()));
    }

    private IntermediateQuery appendSuffixToVariableNames(QueryTransformerFactory transformerFactory,
                                                                 IntermediateQuery query, int suffix) {
        Map<Variable, Variable> substitutionMap =
                query.getKnownVariables().stream()
                        .collect(Collectors.toMap(
                                v -> v,
                                v -> termFactory.getVariable(v.getName()+"m"+suffix)));
        QueryRenamer queryRenamer = transformerFactory.createRenamer(substitutionFactory.getInjectiveVar2VarSubstitution(substitutionMap));
        return queryRenamer.transform(query);
    }
}
