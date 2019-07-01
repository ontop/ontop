package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.impl.DatalogRule2QueryConverter;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.Relation2Predicate;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Legacy code to infer datatypes not declared in the targets of mapping assertions.
 * Types are inferred from the DB metadata.
 * TODO: rewrite in a Datalog independent fashion
 */
public class LegacyMappingDatatypeFiller implements MappingDatatypeFiller {


    private final OntopMappingSettings settings;
    private final Relation2Predicate relation2Predicate;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final TermTypeInferenceTools termTypeInferenceTools;
    private final ImmutabilityTools immutabilityTools;
    private final QueryUnionSplitter unionSplitter;
    private final IQ2DatalogTranslator iq2DatalogTranslator;
    private final UnionFlattener unionNormalizer;
    private final IntermediateQueryFactory iqFactory;
    private final ProvenanceMappingFactory provMappingFactory;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final DatalogRule2QueryConverter datalogRule2QueryConverter;

    @Inject
    private LegacyMappingDatatypeFiller(OntopMappingSettings settings, Relation2Predicate relation2Predicate,
                                        TermFactory termFactory, TypeFactory typeFactory,
                                        TermTypeInferenceTools termTypeInferenceTools, ImmutabilityTools immutabilityTools, QueryUnionSplitter unionSplitter, IQ2DatalogTranslator iq2DatalogTranslator, UnionFlattener unionNormalizer, IntermediateQueryFactory iqFactory, ProvenanceMappingFactory provMappingFactory, NoNullValueEnforcer noNullValueEnforcer, DatalogRule2QueryConverter datalogRule2QueryConverter) {
        this.settings = settings;
        this.relation2Predicate = relation2Predicate;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.termTypeInferenceTools = termTypeInferenceTools;
        this.immutabilityTools = immutabilityTools;
        this.unionSplitter = unionSplitter;
        this.iq2DatalogTranslator = iq2DatalogTranslator;
        this.unionNormalizer = unionNormalizer;
        this.iqFactory = iqFactory;
        this.provMappingFactory = provMappingFactory;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.datalogRule2QueryConverter = datalogRule2QueryConverter;
    }

    /***
     * Infers missing data types.
     * For each rule, gets the type from the rule head, and if absent, retrieves the type from the metadata.
     *
     * The behavior of type retrieval is the following for each rule:
     * . build a "termOccurrenceIndex", which is a map from variables to body atoms + position.
     * For ex, consider the rule: C(x, y) <- A(x, y) \wedge B(y)
     * The "termOccurrenceIndex" map is {
     *  x \mapsTo [<A(x, y), 1>],
     *  y \mapsTo [<A(x, y), 2>, <B(y), 1>]
     *  }
     *  . then take the first occurrence each variable (e.g. take <A(x, y), 2> for variable y),
     *  and assign to the variable the corresponding column type in the DB
     *  (e.g. for y, the type of column 1 of table A).
     *  . then inductively infer the types of functions (e.g. concat, ...) from the variable types.
     *  Only the outermost expression is assigned a type.
     *
     *  Assumptions:
     *  .rule body atoms are extensional
     *  .the corresponding column types are compatible (e.g the types for column 1 of A and column 1 of B)
     */
    @Override
    public MappingWithProvenance inferMissingDatatypes(MappingWithProvenance mapping, DBMetadata dbMetadata) throws UnknownDatatypeException {
        MappingDataTypeCompletion typeCompletion = new MappingDataTypeCompletion(dbMetadata,
                settings.isDefaultDatatypeInferred(), relation2Predicate, termFactory, typeFactory, termTypeInferenceTools, immutabilityTools);

        ImmutableMap<CQIE, PPMappingAssertionProvenance> ruleMap = mapping.getProvenanceMap().entrySet().stream()
                .flatMap(e -> convertMappingQuery(e.getKey())
                        .map(r -> new AbstractMap.SimpleEntry<>(r, e.getValue())))
                .collect(ImmutableCollectors.toMap());

        //CQIEs are mutable
        for(CQIE rule : ruleMap.keySet()) {
            typeCompletion.insertDataTyping(rule);
        }

        ImmutableMap<IQ, PPMappingAssertionProvenance> iqMap = ruleMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> convertDatalogRule(e.getKey()),
                        Map.Entry::getValue));

        return provMappingFactory.create(iqMap, mapping.getMetadata());
    }

    private Stream<CQIE> convertMappingQuery(IQ mappingQuery) {
        return unionSplitter.splitUnion(unionNormalizer.optimize(mappingQuery))
                .flatMap(q -> iq2DatalogTranslator.translate(q).getRules().stream())
                .collect(ImmutableCollectors.toSet())
                .stream();
    }

    private IQ convertDatalogRule(CQIE datalogRule) {

        IQ directlyConvertedIQ = datalogRule2QueryConverter.extractPredicatesAndConvertDatalogRule(
                datalogRule, iqFactory);

        return noNullValueEnforcer.transform(directlyConvertedIQ)
                .liftBinding();
    }

}
