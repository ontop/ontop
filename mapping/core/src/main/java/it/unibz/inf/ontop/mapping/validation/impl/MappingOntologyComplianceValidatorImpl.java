package it.unibz.inf.ontop.mapping.validation.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.impl.PredicateImpl;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.TermType;
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
     *      --> Check according to the building expression of the triple object
     *    - Check the datatypes
     */
    private boolean validateAssertion(IntermediateQuery mappingAssertion, PPTriplesMapProvenance provenance,
                                      ImmutableOntologyVocabulary ontologySignature, TBoxReasoner tBox)
            throws MappingOntologyMismatchException {

        String predicateIRI = extractPredicateIRI(mappingAssertion);

        TermType tripleObjectType = extractTripleObjectType(mappingAssertion);
        Optional<Mismatch> mismatch = lookForMismatch(predicateIRI, tripleObjectType, ontologySignature, tBox);

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

    private String extractPredicateIRI(IntermediateQuery mappingAssertion) {
        AtomPredicate projectionAtomPredicate = mappingAssertion.getProjectionAtom().getPredicate();
        if (projectionAtomPredicate.equals(PredicateImpl.QUEST_TRIPLE_PRED))
            throw new RuntimeException("TODO: extract the RDF predicate from a triple atom");
        else
            return projectionAtomPredicate.getName();
    }

    private TermType extractTripleObjectType(IntermediateQuery mappingAssertion) throws TripleObjectTypeInferenceException {
        throw new RuntimeException("TODO: implement");
    }

    private Optional<Mismatch> lookForMismatch(String predicateIRI, TermType tripleObjectType,
                                               ImmutableOntologyVocabulary ontologySignature, TBoxReasoner tBox) {

        switch (tripleObjectType.getColType()) {
            case UNSUPPORTED:
            case NULL:
                throw new UndeterminedTripleObjectType(predicateIRI, tripleObjectType);
            // Object-like property
            case OBJECT:
            case BNODE:
                return checkObjectOrAnnotationProperty(predicateIRI, ontologySignature, tBox);
            // Data-like property
            default:
                return checkDataOrAnnotationProperty(tripleObjectType, predicateIRI, ontologySignature, tBox);
        }
    }

    /**
     * TODO: what about bootstrapped ontology from the mapping when an annotation property is both
     * used as a object-like and data-like property?
     */
    private Optional<Mismatch> checkObjectOrAnnotationProperty(String predicateIRI,
                                                               ImmutableOntologyVocabulary ontologySignature,
                                                               TBoxReasoner tBox) {
        /*
         * Annotation properties are CURRENTLY assumed to be EITHER used as object or data property
         * TODO: should we only consider the Tbox?
         */
        if (ontologySignature.containsDataProperty(predicateIRI))
            throw new RuntimeException("TODO: create a mismatch or throw an exception");
        else
            return Optional.empty();
    }

    private Optional<Mismatch> checkDataOrAnnotationProperty(TermType tripleObjectType, String predicateIRI,
                                                             ImmutableOntologyVocabulary ontologySignature,
                                                             TBoxReasoner tBox) {
        throw new RuntimeException("TODO: implement it");
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

    @Deprecated
    private class Mismatch {

        private final String predicateName;
        private final Predicate.COL_TYPE typeInMapping;
        private final Predicate.COL_TYPE typeInOntology;

        private Mismatch(String predicateName, Predicate.COL_TYPE typeInMapping, Predicate.COL_TYPE typeInOntology) {
            this.predicateName = predicateName;
            this.typeInMapping = typeInMapping;
            this.typeInOntology = typeInOntology;
        }

        private String getPredicateName() {
            return predicateName;
        }

        private Predicate.COL_TYPE getTypeInMapping() {
            return typeInMapping;
        }

        private Predicate.COL_TYPE getTypeInOntology() {
            return typeInOntology;
        }
    }

    @Deprecated
    enum PredicateType {
        CLASS,
        OBJECT_PROPERTY,
        ANNOTATION_PROPERTY,
        DATATYPE_PROPERTY,
        TRIPLE_PREDICATE
    }

    private static class TripleObjectTypeInferenceException extends OntopInternalBugException {
        TripleObjectTypeInferenceException(IntermediateQuery mappingAssertion, Variable tripleObjectVariable) {
            super("Internal bug: cannot infer the type of " + tripleObjectVariable + " in: \n" + mappingAssertion);
        }
    }

    private static class UndeterminedTripleObjectType extends OntopInternalBugException {
        UndeterminedTripleObjectType(String predicateName, TermType tripleObjectType) {
            super("Internal bug: undetermined type (" + tripleObjectType.getColType() + ") for " + predicateName);
        }
    }
}
