package it.unibz.inf.ontop.mapping.validation.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pp.PPTriplesMapProvenance;
import it.unibz.inf.ontop.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static it.unibz.inf.ontop.mapping.validation.impl.MappingOntologyComplianceValidatorImpl.PredicateType.*;

public class MappingOntologyComplianceValidatorImpl implements MappingOntologyComplianceValidator {

    @Inject
    private MappingOntologyComplianceValidatorImpl() {
    }

    private final ImmutableMap<PredicateType, String> PredicateTypeToErrorMessageSubstring =
            ImmutableMap.<PredicateType, String>builder()
                    .put(CLASS, "a Class")
                    .put(DATATYPE_PROPERTY, "a Datatype Property")
                    .put(ANNOTATION_PROPERTY, "an Annotation Property")
                    .put(OBJECT_PROPERTY, "an Object Property")
                    .build();


    @Override
    public boolean validate(MappingWithProvenance mapping, ImmutableOntologyVocabulary signature,
                            TBoxReasoner tBox)
            throws MappingOntologyMismatchException {


        for (Map.Entry<IntermediateQuery, PPTriplesMapProvenance> entry : mapping.getProvenanceMap().entrySet()) {
            validateAssertion(entry.getKey(), entry.getValue(), signature, tBox);
        }
        return true;
    }

    /**
     * TODO: refactor:
     *    - Only rely on the predicate name, not on its other methods
     *      --> Check according to the building expression of the "object/literal"
     *    - Check the datatypes
     */
    private boolean validateAssertion(IntermediateQuery mappingAssertion, PPTriplesMapProvenance provenance,
                                      ImmutableOntologyVocabulary ontologySignature, TBoxReasoner tBox)
            throws MappingOntologyMismatchException {

        AtomPredicate predicate = mappingAssertion.getProjectionAtom().getPredicate();

        PredicateType predicateType = getPredicateType(predicate);
        Optional<Mismatch> mismatch = lookForMismatch(predicate.getName(), ontologySignature, tBox, predicateType);

        if (mismatch.isPresent()) {
            throw new MappingOntologyMismatchException(
                    generateMisMatchMessage(
                            mappingAssertion.getProjectionAtom(),
                            provenance,
                            mismatch.get()
                    ));
        }
        return true;
    }

    private PredicateType getPredicateType(AtomPredicate predicate) {
        if (predicate.isClass()) {
            return CLASS;
        }
        if (predicate.isDataProperty()) {
            return DATATYPE_PROPERTY;
        }
        if (predicate.isObjectProperty()) {
            return OBJECT_PROPERTY;
        }
        if (predicate.isAnnotationProperty()) {
            return ANNOTATION_PROPERTY;
        }
        if (predicate.isTriplePredicate()) {
            return TRIPLE_PREDICATE;
        }
        else {
            throw new UnexpectedMappingAtomPredicate(predicate);
        }
    }

    private Optional<Mismatch> lookForMismatch(String predicateName, ImmutableOntologyVocabulary ontologySignature,
                                               TBoxReasoner tBox, PredicateType typeInMapping) {

        if (typeInMapping == TRIPLE_PREDICATE) {
            return Optional.empty();
        }

        if (typeInMapping != DATATYPE_PROPERTY &&
                typeInMapping != ANNOTATION_PROPERTY &&
                ontologySignature.containsDataProperty(predicateName)) {
            return Optional.of(
                    new Mismatch(
                            predicateName,
                            typeInMapping,
                            DATATYPE_PROPERTY
                    ));
        }
        if (typeInMapping != OBJECT_PROPERTY &&
                typeInMapping != ANNOTATION_PROPERTY &&
                ontologySignature.containsObjectProperty(predicateName)) {
            return Optional.of(
                    new Mismatch(
                            predicateName,
                            typeInMapping,
                            OBJECT_PROPERTY
                    ));
        }
        if (typeInMapping != CLASS &&
                ontologySignature.containsClass(predicateName)) {
            return Optional.of(
                    new Mismatch(
                            predicateName,
                            typeInMapping,
                            CLASS
                    ));
        }
        if (typeInMapping != ANNOTATION_PROPERTY &&
                typeInMapping != DATATYPE_PROPERTY &&
                typeInMapping != OBJECT_PROPERTY &&
                ontologySignature.containsAnnotationProperty(predicateName)) {
            return Optional.of(
                    new Mismatch(
                            predicateName,
                            typeInMapping,
                            ANNOTATION_PROPERTY
                    ));
        }


        return Optional.empty();
    }

    private String generateMisMatchMessage(DataAtom targetAtom, PPTriplesMapProvenance provenance, Mismatch mismatch) {

        return mismatch.getPredicateName() +
                " is used both as " +
                PredicateTypeToErrorMessageSubstring.get(mismatch.getTypeInOntology()) +
                " in the ontology, and as " +
                PredicateTypeToErrorMessageSubstring.get(mismatch.getTypeInMapping()) +
                " in target atom " +
                targetAtom +
                " of the triplesMap: \n[\n" +
                provenance.getProvenanceInfo() +
                "\n]";
    }

    /**
     * Produces a map from datatypeProperty to corresponding datatype according to the ontology (the datatype may
     * be inferred).
     * This is a rewriting of method:
     * it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.MappingDataTypeRepair#getDataTypeFromOntology
     * from Ontop v 1.18.1
     */
    private ImmutableMap<Predicate, Datatype> computePredicateToDataTypeMap(TBoxReasoner reasoner) {

        // TODO: switch to guava > 2.1, and replace by Streams.stream(iterable)
        return StreamSupport.stream(reasoner.getDataRangeDAG().spliterator(), false)
                .flatMap(n -> getPartialPredicateToDatatypeMap(n, reasoner).entrySet().stream())
                .collect(ImmutableCollectors.toMap());
    }


    private ImmutableMap<Predicate, Datatype> getPartialPredicateToDatatypeMap(Equivalences<DataRangeExpression> nodeSet, TBoxReasoner reasoner) {

        DataRangeExpression node = nodeSet.getRepresentative();

        return ImmutableMap.<Predicate, Datatype>builder()
                .putAll(getEquivalentNodesPartialMap(node, nodeSet))
                .putAll(getDescendentNodesPartialMap(reasoner, node, nodeSet))
                .build();
    }

    private ImmutableMap<Predicate, Datatype> getDescendentNodesPartialMap(TBoxReasoner reasoner, DataRangeExpression node, Equivalences<DataRangeExpression> nodeSet) {
        if (node instanceof Datatype) {
            return reasoner.getDataRangeDAG().getSub(nodeSet).stream()
                    .map(Equivalences::getRepresentative)
                    .filter(d -> d != node)
                    .map(this::getPredicate)
                    .filter(Optional::isPresent)
                    .collect(ImmutableCollectors.toMap(
                            Optional::get,
                            d -> (Datatype) node
                    ));
        }
        return ImmutableMap.of();
    }

    private ImmutableMap<Predicate, Datatype> getEquivalentNodesPartialMap(DataRangeExpression node, Equivalences<DataRangeExpression> nodeSet) {
        ImmutableMap.Builder<Predicate, Datatype> builder = ImmutableMap.builder();
        for (DataRangeExpression equivalent : nodeSet) {
            if (equivalent != node) {
                if (equivalent instanceof Datatype) {
                    getPredicate(node)
                            .ifPresent(p -> builder.put(p, (Datatype) equivalent));
                }
                if (node instanceof Datatype) {
                    getPredicate(equivalent)
                            .ifPresent(p -> builder.put(p, (Datatype) node));
                }
            }
        }
        return builder.build();
    }


    //TODO: check whether the DataRange expression can be neither a Datatype nor a DataPropertyRangeExpression:
    // if the answer is no, drop the Optional and throw an exception instead
    private Optional<Predicate> getPredicate(DataRangeExpression expression) {
        if (expression instanceof Datatype) {
            return Optional.of(((Datatype) expression).getPredicate());
        }
        if (expression instanceof DataPropertyRangeExpression) {
            return Optional.of(((DataPropertyRangeExpression) expression).getProperty().getPredicate());
        }
        return Optional.empty();
    }

    private class Mismatch {

        private final String predicateName;
        private final PredicateType typeInMapping;
        private final PredicateType typeInOntology;

        private Mismatch(String predicateName, PredicateType typeInMapping, PredicateType typeInOntology) {
            this.predicateName = predicateName;
            this.typeInMapping = typeInMapping;
            this.typeInOntology = typeInOntology;
        }

        private String getPredicateName() {
            return predicateName;
        }

        private PredicateType getTypeInMapping() {
            return typeInMapping;
        }

        private PredicateType getTypeInOntology() {
            return typeInOntology;
        }
    }

    enum PredicateType {
        CLASS,
        OBJECT_PROPERTY,
        ANNOTATION_PROPERTY,
        DATATYPE_PROPERTY,
        TRIPLE_PREDICATE
    }

    private static class UnexpectedMappingAtomPredicate extends OntopInternalBugException {
        UnexpectedMappingAtomPredicate(AtomPredicate predicate) {
            super("Cannot categorize this unexpected predicate: " + predicate);
        }
    }
}
