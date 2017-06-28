package it.unibz.inf.ontop.mapping.pp.validation.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.mapping.pp.PreProcessedTriplesMap;
import it.unibz.inf.ontop.mapping.pp.validation.PPMappingOntologyComplianceValidator;
import it.unibz.inf.ontop.model.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.mapping.pp.validation.impl.PPMappingOntologyComplianceValidatorImpl.PredicateType.*;


public class PPMappingOntologyComplianceValidatorImpl implements PPMappingOntologyComplianceValidator {

    public enum PredicateType {
        CLASS,
        OBJECT_PROPERTY,
        ANNOTATION_PROPERTY,
        DATATYPE_PROPERTY,
        TRIPLE_PREDICATE
    };

    private ImmutableMap<PredicateType, String> PredicateTypeToErrorMessageSubstring =
            ImmutableMap.<PredicateType, String>builder()
                    .put(CLASS, "a Class")
                    .put(DATATYPE_PROPERTY, "a Datatype Property")
                    .put(ANNOTATION_PROPERTY, "an Annotation Property")
                    .put(OBJECT_PROPERTY, "an Object Property")
                    .build();

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

    public boolean validate(PreProcessedMapping preProcessedMapping, Ontology ontology) throws
            MappingOntologyMismatchException {
        return preProcessedMapping.getTripleMaps().stream()
                .allMatch(t -> validate((PreProcessedTriplesMap) t, ontology.getVocabulary()));
    }

    private boolean validate(PreProcessedTriplesMap triplesMap, ImmutableOntologyVocabulary
            ontologySignature) {
        return triplesMap.getTargetAtoms().stream()
                .allMatch(f -> validate(triplesMap, f, ontologySignature));
    }

    private boolean validate(PreProcessedTriplesMap triplesMap, ImmutableFunctionalTerm targetTriple, ImmutableOntologyVocabulary ontologySignature) throws MappingOntologyMismatchException {
        String predicateName = targetTriple.getFunctionSymbol().getName();
        Optional<Mismatch> mismatch = lookForMismatch(
                predicateName,
                ontologySignature,
                getPredicateType(targetTriple)
        );

        if (mismatch.isPresent()) {
            throw new MappingOntologyMismatchException(
                    generateMisMatchMessage(
                            targetTriple,
                            triplesMap,
                            mismatch.get()
                    ));
        }
        return true;
    }

    private PredicateType getPredicateType(ImmutableFunctionalTerm targetTriple) {

        Predicate predicate = targetTriple.getFunctionSymbol();
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
        throw new IllegalArgumentException("Unexpected type for predicate: " + predicate + " in target triple " + targetTriple);
    }

    private Optional<Mismatch> lookForMismatch(String predicateName, ImmutableOntologyVocabulary ontologySignature, PredicateType typeInMapping) {

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

    private String generateMisMatchMessage(ImmutableFunctionalTerm targetTriple, PreProcessedTriplesMap triplesMap, Mismatch mismatch) {

        return mismatch.getPredicateName() +
                " is used both as " +
                PredicateTypeToErrorMessageSubstring.get(mismatch.getTypeInOntology()) +
                " in the ontology, and as " +
                PredicateTypeToErrorMessageSubstring.get(mismatch.getTypeInMapping()) +
                " in target atom " +
                targetTriple +
                " of the triplesMap: \n[\n" +
                triplesMap.getProvenance(targetTriple).getProvenanceInfo() +
                "\n]";
    }

    /**
     * Produces a map from datatypeProperty to corresponding datatype according to the ontology (the datatype may
     * be
     * inferred)
     */
    private static ImmutableMap<Predicate, Datatype> getDataTypeFromOntology(TBoxReasoner reasoner){

        final ImmutableMap.Builder<Predicate, Datatype> dataTypesMap = ImmutableMap.builder();

        // Traverse the graph searching for dataProperty
        for (Equivalences<DataRangeExpression> nodes : reasoner.getDataRangeDAG()) {
            DataRangeExpression node = nodes.getRepresentative();

            for (Equivalences<DataRangeExpression> descendants : reasoner.getDataRangeDAG().getSub(nodes)) {
                DataRangeExpression descendant = descendants.getRepresentative();
                if (descendant != node)
                    dataTypesMap.put(onDataRangeInclusion(descendant, node);
            }
            for (DataRangeExpression equivalent : nodes) {
                if (equivalent != node) {
                    onDataRangeInclusion(dataTypesMap, node, equivalent);
                    onDataRangeInclusion(dataTypesMap, equivalent, node);
                }
            }
        }
        return dataTypesMap;
    }

    private static void onDataRangeInclusion(Map<Predicate, Datatype> dataTypesMap, DataRangeExpression sub,
                                             DataRangeExpression sup){
        //if sup is a datatype property we store it in the map
        //it means that sub is of datatype sup
        if (sup instanceof Datatype) {
            Datatype supDataType = (Datatype)sup;
            Predicate key;
            if (sub instanceof Datatype) {
                // datatype inclusion
                key = ((Datatype)sub).getPredicate();
            }
            else if (sub instanceof DataPropertyRangeExpression) {
                // range
                key = ((DataPropertyRangeExpression)sub).getProperty().getPredicate();
            }
            else
                return;

            if (dataTypesMap.containsKey(key))
                throw new PredicateRedefinitionException("Predicate " + key + " with " + dataTypesMap.get(key)
                        + " is redefined as " + supDataType + " in the ontology");
            dataTypesMap.put(key, supDataType);
        }
    }
}
