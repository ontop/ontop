package it.unibz.inf.ontop.mapping.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.MappingConversionException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.ConstructionNodeCleaner;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.FixedPointBindingLiftOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.FlattenUnionOptimizer;
import it.unibz.inf.ontop.pp.PPTriplesMapProvenance;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.datalog.impl.DatalogRule2QueryConverter.convertDatalogRule;

/**
 * Convert mapping assertions from Datalog to IntermediateQuery
 *
 */
@Singleton
public class Datalog2QueryMappingConverterImpl implements Datalog2QueryMappingConverter {

    private final DatalogProgram2QueryConverter converter;
    private final SpecificationFactory specificationFactory;
    private final IntermediateQueryFactory iqFactory;
    private final ProvenanceMappingFactory provMappingFactory;
    private final FixedPointBindingLiftOptimizer bindingLifter;
    private final ConstructionNodeCleaner constructionNodeCleaner;
    private final FlattenUnionOptimizer unionFlattener;

    @Inject
    private Datalog2QueryMappingConverterImpl(DatalogProgram2QueryConverter converter,
                                              SpecificationFactory specificationFactory,
                                              IntermediateQueryFactory iqFactory,
                                              ProvenanceMappingFactory provMappingFactory,
                                              FixedPointBindingLiftOptimizer bindingLifter,
                                              ConstructionNodeCleaner constructionNodeCleaner,
                                              FlattenUnionOptimizer unionFlattener) {
        this.converter = converter;
        this.specificationFactory = specificationFactory;
        this.iqFactory = iqFactory;
        this.provMappingFactory = provMappingFactory;
        this.bindingLifter = bindingLifter;
        this.constructionNodeCleaner = constructionNodeCleaner;
        this.unionFlattener = unionFlattener;
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
                .flatMap(Datalog2QueryTools::extractPredicates)
                .filter(p -> !ruleIndex.containsKey(p))
                .collect(ImmutableCollectors.toSet());

        Stream<IntermediateQuery> mappingStream = ruleIndex.keySet().stream()
                .map(predicate -> converter.convertDatalogDefinitions(dbMetadata,
                        predicate, ruleIndex, extensionalPredicates, Optional.empty(), executorRegistry))
                .filter(Optional::isPresent)
                .map(query -> normalizeIQ(query.get()));

        ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap = mappingStream
                .collect(ImmutableCollectors.toMap(
                        q -> q.getProjectionAtom().getPredicate(),
                        q -> q));

        return specificationFactory.createMapping(mappingMetadata, mappingMap, executorRegistry);
    }

    @Override
    public MappingWithProvenance convertMappingRules(ImmutableMap<CQIE, PPTriplesMapProvenance> datalogMap,
                                                     DBMetadata dbMetadata, ExecutorRegistry executorRegistry,
                                                     MappingMetadata mappingMetadata) {
        ImmutableSet<Predicate> extensionalPredicates = datalogMap.keySet().stream()
                .flatMap(r -> r.getBody().stream())
                .flatMap(Datalog2QueryTools::extractPredicates)
                .collect(ImmutableCollectors.toSet());


        ImmutableMap<IntermediateQuery, PPTriplesMapProvenance> iqMap = datalogMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> convertDatalogRule(dbMetadata, e.getKey(), extensionalPredicates, Optional.empty(),
                                iqFactory, executorRegistry),
                        Map.Entry::getValue));

        return provMappingFactory.create(iqMap, mappingMetadata, executorRegistry);
    }

    /**
     * Lift substitutions and query modifiers, and get rid of resulting idle construction nodes.
     * Then flatten nested unions.
     */
    private IntermediateQuery normalizeIQ(IntermediateQuery query) {

        try {
            IntermediateQuery queryAfterBindingLift = bindingLifter.optimize(query);
            IntermediateQuery queryAfterCNodeCleaning = constructionNodeCleaner.optimize(queryAfterBindingLift);
            return unionFlattener.optimize(queryAfterCNodeCleaning);
        } catch (EmptyQueryException e) {
            throw new MappingConversionException(e);
        }
    }
}
