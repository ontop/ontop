package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.Relation2Predicate;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;

/**
 * Legacy code to infer datatypes not declared in the targets of mapping assertions.
 * Types are inferred from the DB metadata.
 * TODO: rewrite in a Datalog independent fashion
 */
public class LegacyMappingDatatypeFiller implements MappingDatatypeFiller {


    private final Datalog2QueryMappingConverter datalog2MappingConverter;
    private final Mapping2DatalogConverter mapping2DatalogConverter;
    private final OntopMappingSettings settings;
    private final Relation2Predicate relation2Predicate;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final TermTypeInferenceTools termTypeInferenceTools;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private LegacyMappingDatatypeFiller(Datalog2QueryMappingConverter datalog2MappingConverter,
                                        Mapping2DatalogConverter mapping2DatalogConverter,
                                        OntopMappingSettings settings, Relation2Predicate relation2Predicate,
                                        TermFactory termFactory, TypeFactory typeFactory,
                                        TermTypeInferenceTools termTypeInferenceTools, ImmutabilityTools immutabilityTools) {
        this.datalog2MappingConverter = datalog2MappingConverter;
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.settings = settings;
        this.relation2Predicate = relation2Predicate;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.termTypeInferenceTools = termTypeInferenceTools;
        this.immutabilityTools = immutabilityTools;
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
        ImmutableMap<CQIE, PPMappingAssertionProvenance> ruleMap = mapping2DatalogConverter.convert(mapping);

        //CQIEs are mutable
        for(CQIE rule : ruleMap.keySet()){
            typeCompletion.insertDataTyping(rule);
        }
        return datalog2MappingConverter.convertMappingRules(ruleMap, dbMetadata,
                mapping.getExecutorRegistry(), mapping.getMetadata());
    }
}
