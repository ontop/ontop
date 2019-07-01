package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;

/**
 * Convert mapping assertions from Datalog to IntermediateQuery
 */
@Singleton
public class Datalog2QueryMappingConverterImpl implements Datalog2QueryMappingConverter {

    private final IntermediateQueryFactory iqFactory;
    private final ProvenanceMappingFactory provMappingFactory;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final DatalogRule2QueryConverter datalogRule2QueryConverter;

    @Inject
    private Datalog2QueryMappingConverterImpl(IntermediateQueryFactory iqFactory,
                                              ProvenanceMappingFactory provMappingFactory,
                                              NoNullValueEnforcer noNullValueEnforcer,
                                              DatalogRule2QueryConverter datalogRule2QueryConverter){
        this.iqFactory = iqFactory;
        this.provMappingFactory = provMappingFactory;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.datalogRule2QueryConverter = datalogRule2QueryConverter;
    }


    @Override
    public MappingWithProvenance convertMappingRules(ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap,
                                                     MappingMetadata mappingMetadata) {

        ImmutableMap<IQ, PPMappingAssertionProvenance> iqMap = datalogMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> convertDatalogRule(e.getKey()),
                        Map.Entry::getValue));

        return provMappingFactory.create(iqMap, mappingMetadata);
    }

    private IQ convertDatalogRule(CQIE datalogRule) {

        ImmutableSet<Predicate> extensionalPredicates = datalogRule.getBody().stream()
                .flatMap(Datalog2QueryTools::extractPredicates)
                .collect(ImmutableCollectors.toSet());

        IQ directlyConvertedIQ = datalogRule2QueryConverter.convertDatalogRule(
                datalogRule, extensionalPredicates, iqFactory);

        return noNullValueEnforcer.transform(directlyConvertedIQ)
                .liftBinding();
    }
}
