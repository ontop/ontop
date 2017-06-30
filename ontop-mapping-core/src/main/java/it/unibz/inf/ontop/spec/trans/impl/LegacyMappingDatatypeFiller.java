package it.unibz.inf.ontop.spec.trans.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.spec.trans.MappingDatatypeFiller;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.MappingDataTypeCompletion;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Legacy code to infer datatypes not declared in the targets of mapping assertions.
 * Types are inferred from the DB metadata.
 * TODO: rewrite in a Datalog independent fashion
 */
public class LegacyMappingDatatypeFiller implements MappingDatatypeFiller {


    private final Datalog2QueryMappingConverter datalog2QueryMappingConverter;
    private final Mapping2DatalogConverter mapping2DatalogConverter;

    @Inject
    private LegacyMappingDatatypeFiller(Datalog2QueryMappingConverter datalog2QueryMappingConverter,
                                        Mapping2DatalogConverter mapping2DatalogConverter) {
        this.datalog2QueryMappingConverter = datalog2QueryMappingConverter;
        this.mapping2DatalogConverter = mapping2DatalogConverter;
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
     *
     *  Assumptions:
     *  .rule body atoms are extensional
     *  .the corresponding column types are compatible (e.g the types for column 1 of A and column 1 of B)
     */
    @Override
    public Mapping inferMissingDatatypes(Mapping mapping, TBoxReasoner tBox, ImmutableOntologyVocabulary
            vocabulary, DBMetadata dbMetadata, ExecutorRegistry executorRegistry) {
        MappingDataTypeCompletion typeCompletion = new MappingDataTypeCompletion(dbMetadata);
        ImmutableList<CQIE> rules = mapping2DatalogConverter.convert(mapping)
                .collect(ImmutableCollectors.toList());
        //CQIEs are mutable
        rules.forEach(r -> typeCompletion.insertDataTyping(r));
        return datalog2QueryMappingConverter.convertMappingRules(ImmutableList.copyOf(rules), dbMetadata,
                executorRegistry,
                mapping.getMetadata());
    }
}
