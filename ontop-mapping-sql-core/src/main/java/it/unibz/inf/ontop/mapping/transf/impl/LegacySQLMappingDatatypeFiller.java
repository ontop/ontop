package it.unibz.inf.ontop.mapping.transf.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.mapping.transf.MappingDatatypeFiller;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.VocabularyValidator;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.MappingDataTypeCompletion;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * Legacy code to infer datatypes not declared in the targets of mapping assertions
 * Currently infers some types from the ontology and/or the DB metadata (see comments of method inferMissingDataTypes
 * for detailed behavior)
 *
 * TODO: rewrite in a Datalog independent fashion, and with the desired behavior (no datatype is inferred from the
 * ontology)
 */
public class LegacySQLMappingDatatypeFiller implements MappingDatatypeFiller {


    private final Datalog2QueryMappingConverter datalog2QueryMappingConverter;
    private final Mapping2DatalogConverter mapping2DatalogConverter;

    @Inject
    public LegacySQLMappingDatatypeFiller(Datalog2QueryMappingConverter datalog2QueryMappingConverter, Mapping2DatalogConverter mapping2DatalogConverter) {
        this.datalog2QueryMappingConverter = datalog2QueryMappingConverter;
        this.mapping2DatalogConverter = mapping2DatalogConverter;

    }


    @Override
    public Mapping inferMissingDatatypes(Mapping mapping, TBoxReasoner tBox, ImmutableOntologyVocabulary
            vocabulary, DBMetadata dbMetadata, ExecutorRegistry executorRegistry) {
        // Adding data typing on the mapping axioms.
        ImmutableList<CQIE> fullyTypedRules = inferMissingDataTypes(
                mapping2DatalogConverter.convert(mapping).collect(ImmutableCollectors.toList()),
                tBox,
                vocabulary,
                dbMetadata
        );
        return datalog2QueryMappingConverter.convertMappingRules(fullyTypedRules, dbMetadata, executorRegistry,
                mapping.getMetadata());
    }

    /***
     * Infers missing data types.
     * For each rule, the behavior is the following:
     * .get the type from the rule head
     * .if present[
     *      .if the type is boolean and the DB is DB2[
     *          retrieve the the type from the metadata instead
     * ]
     * .if absent[
     *     .get the type from the ontology
     *     .if absent or (present and boolean and the DB is DB2) [
     *          retrieve the type from the metadata
     *     ]
     * ]
     *
     * The behavior of type retrieval is the following for each rule
     * . Build a "termOccurrenceIndex", which is a map from variables to body atoms + position.
     * For ex, consider the rule: C(x, y) <- A(x, y) \wedge B(y)
     * The "termOccurrenceIndex" map is {
     *  x \mapsTo [<A(x, y), 1>],
     *  y \mapsTo [<A(x, y), 2>, <B(y), 1>]
     *  }
     *  Then take the first occurrence each variable (e.g. take <A(x, y), 2> for variable y),
     *  and assign to the variable the corresponding column type in the DB
     *  (e.g. for y, the type of column 1 of table A).
     *
     *  Assumptions:
     *  .rule body atoms are extensional
     *  .the corresponding column types are compatible (e.g the types for column 1 of A and column 1 of B)
     */
    public ImmutableList<CQIE> inferMissingDataTypes(ImmutableList<CQIE> unfoldingProgram, TBoxReasoner tBoxReasoner,
                                                     ImmutableOntologyVocabulary vocabulary, DBMetadata metadata) throws MappingException {

        VocabularyValidator vocabularyValidator = new VocabularyValidator(tBoxReasoner, vocabulary);

        MappingDataTypeCompletion typeRepair = new MappingDataTypeCompletion(metadata, tBoxReasoner, vocabularyValidator);
        // TODO: create a new program (with fresh rules), instead of modifying each rule ?
        unfoldingProgram.forEach(r -> typeRepair.insertDataTyping(r));
        return unfoldingProgram;
    }
}
