package it.unibz.inf.ontop.pp.validation.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pp.PreProcessedMapping;
import it.unibz.inf.ontop.pp.PreProcessedTriplesMap;
import it.unibz.inf.ontop.pp.validation.PPMappingOntologyComplianceValidator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static it.unibz.inf.ontop.pp.validation.impl.PPMappingOntologyComplianceValidatorImpl.PredicateType.*;


public class PPMappingOntologyComplianceValidatorImpl implements PPMappingOntologyComplianceValidator {

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

    public enum PredicateType {
        CLASS,
        OBJECT_PROPERTY,
        ANNOTATION_PROPERTY,
        DATATYPE_PROPERTY,
        TRIPLE_PREDICATE
    };

    private final ImmutableMap<PredicateType, String> PredicateTypeToErrorMessageSubstring =
            ImmutableMap.<PredicateType, String>builder()
                    .put(CLASS, "a Class")
                    .put(DATATYPE_PROPERTY, "a Datatype Property")
                    .put(ANNOTATION_PROPERTY, "an Annotation Property")
                    .put(OBJECT_PROPERTY, "an Object Property")
                    .build();



    @Override
    public boolean validateMapping(PreProcessedMapping preProcessedMapping, ImmutableOntologyVocabulary signature,
                                   TBoxReasoner tBox)
            throws MappingOntologyMismatchException {
        ImmutableMap<Predicate, Datatype> predicate2DatatypeMap = computePredicateToDataTypeMap(tBox);
        return preProcessedMapping.getTripleMaps().stream()
                .allMatch(t -> validateTriplesMap((PreProcessedTriplesMap) t, signature, tBox));
    }

    private boolean validateTriplesMap(PreProcessedTriplesMap triplesMap, ImmutableOntologyVocabulary ontologySignature, TBoxReasoner tBox) {
        return triplesMap.getTargetAtoms().stream()
                .allMatch(f -> validateTriple(triplesMap, f, ontologySignature, tBox));
    }

    private boolean validateTriple(PreProcessedTriplesMap triplesMap, ImmutableFunctionalTerm targetTriple, ImmutableOntologyVocabulary ontologySignature, TBoxReasoner tBox)
            throws MappingOntologyMismatchException {
        String predicateName = targetTriple.getFunctionSymbol().getName();
        Optional<PredicateType> predicateType = getPredicateType(targetTriple);
        Optional<Mismatch> mismatch = predicateType.isPresent()?
                lookForMismatch(predicateName, ontologySignature, tBox, predicateType.get()):
                Optional.empty();

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

    private Optional<PredicateType> getPredicateType(ImmutableFunctionalTerm targetTriple) {

        Predicate predicate = targetTriple.getFunctionSymbol();
        if (predicate.isClass()) {
            return Optional.of(CLASS);
        }
        if (predicate.isDataProperty()) {
            return Optional.of(DATATYPE_PROPERTY);
        }
        if (predicate.isObjectProperty()) {
            return Optional.of(OBJECT_PROPERTY);
        }
        if (predicate.isAnnotationProperty()) {
            return Optional.of(ANNOTATION_PROPERTY);
        }
        if (predicate.isTriplePredicate()) {
            return Optional.of(TRIPLE_PREDICATE);
        }
        return Optional.empty();
//        throw new IllegalArgumentException("Unexpected type for predicate: " + predicate + " in target triple " + targetTriple);
    }

    private Optional<Mismatch> lookForMismatch(String predicateName, ImmutableOntologyVocabulary ontologySignature, TBoxReasoner tBox, PredicateType typeInMapping) {

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
                    .map(s -> s.getRepresentative())
                    .filter(d -> d != node)
                    .map(d -> getPredicate(d))
                    .filter(d -> d.isPresent())
                    .collect(ImmutableCollectors.toMap(
                            d -> d.get(),
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
}
