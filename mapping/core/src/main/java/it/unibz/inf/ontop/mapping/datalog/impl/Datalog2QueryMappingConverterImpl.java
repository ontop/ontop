package it.unibz.inf.ontop.mapping.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.predicate.AlgebraOperatorPredicate;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.OperationPredicate;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Convert mapping assertions from Datalog to IntermediateQuery
 *
 */
@Singleton
public class Datalog2QueryMappingConverterImpl implements Datalog2QueryMappingConverter {

    private final DatalogProgram2QueryConverter converter;
    private final SpecificationFactory specificationFactory;

    @Inject
    private Datalog2QueryMappingConverterImpl(DatalogProgram2QueryConverter converter,
                                              SpecificationFactory specificationFactory) {
        this.converter = converter;
        this.specificationFactory = specificationFactory;
    }

    @Override
    public Mapping convertMappingRules(ImmutableList<CQIE> mappingRules, DBMetadata dbMetadata,
                                       ExecutorRegistry executorRegistry, MappingMetadata mappingMetadata) {

        ImmutableMultimap<Predicate, CQIE> ruleIndex = mappingRules.stream()
                .collect(ImmutableCollectors.toMultimap(
                        r -> r.getHead().getFunctionSymbol(),
                        r -> r
                ));

        ImmutableSet<Predicate> extensionalPredicates = ruleIndex.values().stream()
                .flatMap(r -> r.getBody().stream())
                .flatMap(Datalog2QueryMappingConverterImpl::extractPredicates)
                .filter(p -> !ruleIndex.containsKey(p))
                .collect(ImmutableCollectors.toSet());

        Stream<IntermediateQuery> mappingStream = ruleIndex.keySet().stream()
                .map(predicate -> converter.convertDatalogDefinitions(dbMetadata,
                        predicate, ruleIndex, extensionalPredicates, Optional.empty(), executorRegistry))
                .filter(Optional::isPresent)
                .map(Optional::get);

        ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap = mappingStream
                .collect(ImmutableCollectors.toMap(
                        q -> q.getProjectionAtom().getPredicate(),
                        q -> q));

        return specificationFactory.createMapping(mappingMetadata, mappingMap, executorRegistry);
    }

    private static Stream<Predicate> extractPredicates(Function atom) {
        Predicate currentpred = atom.getFunctionSymbol();
        if (currentpred instanceof OperationPredicate)
            return Stream.of();
        else if (currentpred instanceof AlgebraOperatorPredicate) {
            return atom.getTerms().stream()
                    .filter(t -> t instanceof Function)
                    // Recursive
                    .flatMap(t -> extractPredicates((Function) t));
        } else {
            return Stream.of(currentpred);
        }
    }


}
