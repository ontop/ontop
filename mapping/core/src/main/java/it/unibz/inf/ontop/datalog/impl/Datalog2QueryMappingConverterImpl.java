package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.AbstractMap;
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
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final DatalogRule2QueryConverter datalogRule2QueryConverter;

    @Inject
    private Datalog2QueryMappingConverterImpl(DatalogProgram2QueryConverter converter,
                                              SpecificationFactory specificationFactory,
                                              IntermediateQueryFactory iqFactory,
                                              ProvenanceMappingFactory provMappingFactory,
                                              NoNullValueEnforcer noNullValueEnforcer,
                                              DatalogRule2QueryConverter datalogRule2QueryConverter){
        this.converter = converter;
        this.specificationFactory = specificationFactory;
        this.iqFactory = iqFactory;
        this.provMappingFactory = provMappingFactory;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.datalogRule2QueryConverter = datalogRule2QueryConverter;
    }

    @Override
    public Mapping convertMappingRules(ImmutableList<CQIE> mappingRules, MappingMetadata mappingMetadata) {

        ImmutableMultimap<Term, CQIE> ruleIndex = mappingRules.stream()
                .collect(ImmutableCollectors.toMultimap(
                        r -> Datalog2QueryTools.isURIRDFType(r.getHead().getTerm(1))
                                ? r.getHead().getTerm(2)
                                : r.getHead().getTerm(1),
                        r -> r
                ));

        ImmutableSet<Predicate> extensionalPredicates = mappingRules.stream()
                .flatMap(r -> r.getBody().stream())
                .flatMap(Datalog2QueryTools::extractPredicates)
                .filter(p -> !ruleIndex.containsKey(p))
                .collect(ImmutableCollectors.toSet());

        ImmutableList<AbstractMap.Entry<MappingTools.RDFPredicateInfo, IQ>> intermediateQueryList = ruleIndex.keySet().stream()
                .map(predicate -> converter.convertDatalogDefinitions(
                        ruleIndex.get(predicate),
                        extensionalPredicates,
                        Optional.empty()
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                // In case some legacy implementations do not preserve IS_NOT_NULL conditions
                .map(noNullValueEnforcer::transform)
                .map(IQ::liftBinding)
                .map(iq -> new AbstractMap.SimpleImmutableEntry<>(MappingTools.extractRDFPredicate(iq), iq))
                .collect(ImmutableCollectors.toList());

        return specificationFactory.createMapping(mappingMetadata,
                extractTable(intermediateQueryList, false),
                extractTable(intermediateQueryList, true));
    }

    private ImmutableTable<RDFAtomPredicate, IRI, IQ> extractTable(
            ImmutableList<Map.Entry<MappingTools.RDFPredicateInfo, IQ>> iqClassificationMap, boolean isClass) {

        return iqClassificationMap.stream()
                .filter(e -> e.getKey().isClass() == isClass)
                .map(e -> Tables.immutableCell(
                        (RDFAtomPredicate) e.getValue().getProjectionAtom().getPredicate(),
                        e.getKey().getIri(),
                        e.getValue()))
                .collect(ImmutableCollectors.toTable());
    }

    @Override
    public MappingWithProvenance convertMappingRules(ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap,
                                                     MappingMetadata mappingMetadata) {

        ImmutableSet<Predicate> extensionalPredicates = datalogMap.keySet().stream()
                .flatMap(r -> r.getBody().stream())
                .flatMap(Datalog2QueryTools::extractPredicates)
                .collect(ImmutableCollectors.toSet());

        ImmutableMap<IQ, PPMappingAssertionProvenance> iqMap = datalogMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> convertDatalogRule(e.getKey(), extensionalPredicates),
                        Map.Entry::getValue));

        return provMappingFactory.create(iqMap, mappingMetadata);
    }

    private IQ convertDatalogRule(CQIE datalogRule, ImmutableSet<Predicate> extensionalPredicates) {
        IQ directlyConvertedIQ = datalogRule2QueryConverter.convertDatalogRule(
                datalogRule,
                extensionalPredicates,
                Optional.empty(),
                iqFactory);

        return noNullValueEnforcer.transform(directlyConvertedIQ)
                .liftBinding();
    }

}
