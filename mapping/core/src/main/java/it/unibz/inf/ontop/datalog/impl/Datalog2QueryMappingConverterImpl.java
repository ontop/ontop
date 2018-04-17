package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.MappingIQNormalizer;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;

/**
 * Convert mapping assertions from Datalog to IntermediateQuery
 */
@Singleton
public class Datalog2QueryMappingConverterImpl implements Datalog2QueryMappingConverter {

    private final DatalogProgram2QueryConverter converter;
    private final SpecificationFactory specificationFactory;
    private final IntermediateQueryFactory iqFactory;
    private final ProvenanceMappingFactory provMappingFactory;
    private final MappingIQNormalizer mappingIQNormalizer;
    private final DatalogRule2QueryConverter datalogRule2QueryConverter;

    @Inject
    private Datalog2QueryMappingConverterImpl(DatalogProgram2QueryConverter converter,
                                              SpecificationFactory specificationFactory,
                                              IntermediateQueryFactory iqFactory,
                                              ProvenanceMappingFactory provMappingFactory,
                                              MappingIQNormalizer mappingIQNormalizer,
                                              DatalogRule2QueryConverter datalogRule2QueryConverter){
        this.converter = converter;
        this.specificationFactory = specificationFactory;
        this.iqFactory = iqFactory;
        this.provMappingFactory = provMappingFactory;
        this.mappingIQNormalizer = mappingIQNormalizer;
        this.datalogRule2QueryConverter = datalogRule2QueryConverter;
    }

    @Override
    public Mapping convertMappingRules(ImmutableList<CQIE> mappingRules, DBMetadata dbMetadata,
                                       ExecutorRegistry executorRegistry, MappingMetadata mappingMetadata) {


        ImmutableMultimap<Term, CQIE> ruleIndex = mappingRules.stream()
                .collect(ImmutableCollectors.toMultimap(
                        r -> Datalog2QueryTools.isURIRDFType(r.getHead().getTerm(1))? r.getHead().getTerm(2) : r.getHead().getTerm(1),
                        r -> r
                ));


        ImmutableSet<Predicate> extensionalPredicates = ruleIndex.values().stream()
                .flatMap(r -> r.getBody().stream())
                .flatMap(Datalog2QueryTools::extractPredicates)
                .filter(p -> !ruleIndex.containsKey(p))
                .collect(ImmutableCollectors.toSet());

        ImmutableList<IQ> intermediateQueryList = ruleIndex.keySet().stream()
                .map(predicate -> converter.convertDatalogDefinitions(
                        ruleIndex.get(predicate),
                        extensionalPredicates,
                        Optional.empty()
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(mappingIQNormalizer::normalize)
                .collect(ImmutableCollectors.toList());

        ImmutableMap<IRI, IQ> mappingClassMap = intermediateQueryList.stream()
                .filter (assertion ->  {
                    ImmutableList<Variable> projectedVariables = assertion.getProjectionAtom().getArguments();
                    IRI predicateIRI =  MappingTools.extractPredicateTerm(assertion, projectedVariables.get(1));
                    return (predicateIRI.equals(RDF.TYPE));})
                .collect(ImmutableCollectors.toMap(
                        MappingTools::extractClassIRI,
                        a -> a));

        ImmutableMap<IRI, IQ> mappingPropertiesMap = intermediateQueryList.stream()
                .filter (assertion ->  {
                    ImmutableList<Variable> projectedVariables = assertion.getProjectionAtom().getArguments();
                    IRI predicateIRI =  MappingTools.extractPredicateTerm(assertion, projectedVariables.get(1));
                    return (!predicateIRI.equals(RDF.TYPE));})
                .collect(ImmutableCollectors.toMap(
                        MappingTools::extractPropertiesIRI,
                        a -> a));


        return specificationFactory.createMapping(mappingMetadata, mappingPropertiesMap, mappingClassMap, executorRegistry);
    }

    @Override
    public MappingWithProvenance convertMappingRules(ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap,
                                                     DBMetadata dbMetadata, ExecutorRegistry executorRegistry,
                                                     MappingMetadata mappingMetadata) {

        ImmutableSet<Predicate> extensionalPredicates = datalogMap.keySet().stream()
                .flatMap(r -> r.getBody().stream())
                .flatMap(Datalog2QueryTools::extractPredicates)
                .collect(ImmutableCollectors.toSet());


        ImmutableMap<IQ, PPMappingAssertionProvenance> iqMap = datalogMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> mappingIQNormalizer.normalize(
                                datalogRule2QueryConverter.convertDatalogRule(
                                        e.getKey(),
                                        extensionalPredicates,
                                        Optional.empty(),
                                        iqFactory
                                )),
                        Map.Entry::getValue
                ));
        return provMappingFactory.create(iqMap, mappingMetadata, executorRegistry);
    }
}
