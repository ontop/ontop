package it.unibz.inf.ontop.mapping.extraction.validation.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedTriplesMap;
import it.unibz.inf.ontop.mapping.extraction.validation.PPMappingOntologyComplianceValidator;
import it.unibz.inf.ontop.model.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;

import java.util.Optional;

import static it.unibz.inf.ontop.mapping.extraction.validation.impl.PPMappingOntologyComplianceValidatorImpl.PredicateType.*;


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

        public Mismatch(String predicateName, PredicateType typeInMapping, PredicateType typeInOntology) {
            this.predicateName = predicateName;
            this.typeInMapping = typeInMapping;
            this.typeInOntology = typeInOntology;
        }

        public String getPredicateName() {
            return predicateName;
        }

        public PredicateType getTypeInMapping() {
            return typeInMapping;
        }

        public PredicateType getTypeInOntology() {
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
}
