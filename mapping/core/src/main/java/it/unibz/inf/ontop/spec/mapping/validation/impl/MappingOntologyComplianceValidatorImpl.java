package it.unibz.inf.ontop.spec.mapping.validation.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


@Singleton
public class MappingOntologyComplianceValidatorImpl implements MappingOntologyComplianceValidator {

    private static final String DATA_PROPERTY_STR = "a data property";
    private static final String OBJECT_PROPERTY_STR = "an object property";
    private static final String ANNOTATION_PROPERTY_STR = "an annotation property";
    private static final String CLASS_STR = "a class";

    private final TypeFactory typeFactory;

    @Inject
    private MappingOntologyComplianceValidatorImpl(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }


    /**
     * Requires the annotation, data and object properties to be clearly distinguished
     *  (disjoint sets, according to the OWL semantics)
     *
     * Be careful if you are using a T-box bootstrapped from the mapping
     *
     * It is NOT assumed that the declared vocabulary contains information on every RDF predicate
     * used in the mapping.
     *
     */
    @Override
    public void validate(ImmutableList<MappingAssertion> mapping, Ontology ontology)
            throws MappingOntologyMismatchException {

        ImmutableMultimap<IRI, Datatype> datatypeMap = computeDataTypeMap(ontology.tbox());

        for (MappingAssertion a : mapping)
            validateAssertion(a, ontology, datatypeMap);
    }

    private void validateAssertion(MappingAssertion assertion,
                                   Ontology ontology,
                                   ImmutableMultimap<IRI, Datatype> datatypeMap)
            throws MappingOntologyMismatchException {

        Optional<RDFTermType> tripleObjectType = assertion.getIndex().isClass()
                ? Optional.empty()
                : extractTripleObjectType(assertion);

        try {
            checkTripleObject(assertion.getIndex().getIri(), tripleObjectType, ontology, datatypeMap);
        }
        catch (MappingOntologyMismatchException e) {
            throw new MappingOntologyMismatchException(e, "\n[\n" + assertion.getProvenance().getProvenanceInfo() + "\n]");
        }
    }



    /**
     * For a mapping assertion using an RDF property (not rdf:type) the building expression of the triple object
     * variable is assumed to be present in the ROOT construction node.
     *
     * Note that this assumption does not hold for intermediate query in general.
     *
     */
    private Optional<RDFTermType> extractTripleObjectType(MappingAssertion assertion)
            throws TripleObjectTypeInferenceException {

        Variable objectVariable = assertion.getRDFAtomPredicate().getObject(assertion.getProjectionAtom().getArguments());
        ImmutableTerm constructionTerm = assertion.getTopSubstitution().get(objectVariable);

        if (constructionTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm constructionFunctionalTerm = ((ImmutableFunctionalTerm) constructionTerm);

            Optional<RDFTermType> optionalType = constructionFunctionalTerm.inferType()
                    .flatMap(TermTypeInference::getTermType)
                    .filter(t -> t instanceof RDFTermType)
                    .map(t -> (RDFTermType) t);

            if (!optionalType.isPresent())
                throw new TripleObjectTypeInferenceException(assertion.getQuery(), objectVariable,
                        "Not defined in the root node (expected for a mapping assertion)");
            return optionalType;
        }
        else if (constructionTerm instanceof RDFConstant) {
            return Optional.of(((RDFConstant) constructionTerm).getType());
        }
        else {
            /*
             * TODO: consider variables (NB: could be relevant for SPARQL->SPARQL
              * but not much for SPARQL->SQL where RDF terms have to built)
             */
            throw new TripleObjectTypeInferenceException(assertion.getQuery(), objectVariable,
                    "Was expecting a functional or constant term (variables are not yet supported). \n"
                            + "Term definition: " + constructionTerm);
        }
    }



    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void checkTripleObject(IRI predicateIRI, Optional<RDFTermType> optionalTripleObjectType,
                                   Ontology ontology,
                                   ImmutableMultimap<IRI, Datatype> datatypeMap)
            throws MappingOntologyMismatchException {

        if (optionalTripleObjectType.isPresent()) {
            RDFTermType tripleObjectType = optionalTripleObjectType.get();

            if (tripleObjectType.isAbstract())
                throw new AbstractTripleObjectTypeException(predicateIRI, tripleObjectType);

            /*
             * TODO: avoid instanceof tests!
             */
            if (tripleObjectType instanceof ObjectRDFType) {
                checkObjectOrAnnotationProperty(predicateIRI, ontology);
            }
            else if (tripleObjectType instanceof RDFDatatype) {
                checkDataOrAnnotationProperty((RDFDatatype)tripleObjectType, predicateIRI, ontology, datatypeMap);
            }
            else {
                // E.g. Unbound
                throw new UndeterminedTripleObjectTypeException(predicateIRI, tripleObjectType);
            }
        }
        else {
            checkClass(predicateIRI, ontology);
        }
    }

    private void checkObjectOrAnnotationProperty(IRI predicateIRI, Ontology ontology)
            throws MappingOntologyMismatchException {
        /*
         * Cannot be a data property (should be either an object or an annotation property)
         */
        if (ontology.tbox().dataProperties().contains(predicateIRI))
            throw new MappingOntologyMismatchException(predicateIRI, DATA_PROPERTY_STR, OBJECT_PROPERTY_STR);
        /*
         * Cannot be a class
         */
        if (ontology.tbox().classes().contains(predicateIRI))
            throw new MappingOntologyMismatchException(predicateIRI, CLASS_STR, OBJECT_PROPERTY_STR);
    }

    private void checkDataOrAnnotationProperty(RDFDatatype tripleObjectType, IRI predicateIRI,
                                               Ontology ontology,
                                               ImmutableMultimap<IRI, Datatype> datatypeMap)
            throws MappingOntologyMismatchException {
        /*
         * Cannot be an object property
         */
        if (ontology.tbox().objectProperties().contains(predicateIRI))
            throw new MappingOntologyMismatchException(predicateIRI, OBJECT_PROPERTY_STR, DATA_PROPERTY_STR);
        /*
         * Cannot be a class
         */
        if (ontology.tbox().classes().contains(predicateIRI))
            throw new MappingOntologyMismatchException(predicateIRI, CLASS_STR, DATA_PROPERTY_STR);

        /*
         * Checks the datatypes
         */
        for (Datatype declaredDatatype : datatypeMap.get(predicateIRI)) {

            if (declaredDatatype.getIRI().equals(RDFS.LITERAL)) {
                break;
            }

            RDFDatatype declaredTermType = typeFactory.getDatatype(declaredDatatype.getIRI());

            if (!tripleObjectType.isA(declaredTermType)) {
                throw new MappingOntologyMismatchException(predicateIRI, declaredDatatype.toString(), tripleObjectType.getIRI().toString());
            }
        }
    }

    private void checkClass(IRI predicateIRI, Ontology ontology) throws MappingOntologyMismatchException {
        /*
         * Cannot be an object property
         */
        if (ontology.tbox().objectProperties().contains(predicateIRI))
            throw new MappingOntologyMismatchException(predicateIRI, OBJECT_PROPERTY_STR, CLASS_STR);
        /*
         * Cannot be a data property
         */
        if (ontology.tbox().dataProperties().contains(predicateIRI))
            throw new MappingOntologyMismatchException(predicateIRI, DATA_PROPERTY_STR, CLASS_STR);

        /*
         * Cannot be an annotation property
         */
        if (ontology.annotationProperties().contains(predicateIRI))
            throw new MappingOntologyMismatchException(predicateIRI, ANNOTATION_PROPERTY_STR, DATA_PROPERTY_STR);
    }


    /**
     * Produces a map from datatypeProperty to corresponding datatype according to the ontology (the datatype may
     * be inferred).
     * This is a rewriting of method:
     * it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.MappingDataTypeRepair#getDataTypeFromOntology
     * from Ontop v 1.18.1
     */
    private static ImmutableMultimap<IRI, Datatype> computeDataTypeMap(ClassifiedTBox reasoner) {
        return reasoner.dataRangesDAG().stream()
                .flatMap(n -> getPartialPredicateToDatatypeMap(n, reasoner.dataRangesDAG()))
                .collect(ImmutableCollectors.toMultimap());
    }


    private static Stream<Map.Entry<IRI, Datatype>> getPartialPredicateToDatatypeMap(Equivalences<DataRangeExpression> nodeSet,
                                                                              EquivalencesDAG<DataRangeExpression> dag) {
        return Stream.concat(
                getSub(dag.getSub(nodeSet).stream()
                        .map(Equivalences::getRepresentative), nodeSet.getRepresentative()),
                getEquivalentNodesPartialMap(nodeSet));
    }

    private static Stream<Map.Entry<IRI, Datatype>> getEquivalentNodesPartialMap(Equivalences<DataRangeExpression> nodeSet) {
        DataRangeExpression node = nodeSet.getRepresentative();
        return Stream.concat(
                getPredicateIRI(node)
                        .map(i -> nodeSet.stream()
                                .filter(e -> e != node)
                                .filter(e -> e instanceof Datatype)
                                .map(e -> (Datatype)e)
                                .map(e -> Maps.immutableEntry(i, e)))
                        .orElse(Stream.of()),
                getSub(nodeSet.stream(), node));
    }

    private static Stream<Map.Entry<IRI, Datatype>> getSub(Stream<DataRangeExpression> stream, DataRangeExpression node) {
        return  Optional.of(node)
                .filter(n -> n instanceof Datatype)
                .map(n -> (Datatype)n)
                .map(n -> stream
                        .filter(e -> e != n)
                        .map(MappingOntologyComplianceValidatorImpl::getPredicateIRI)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(i -> Maps.immutableEntry(i, n)))
                .orElse(Stream.of());
    }

    //TODO: check whether the DataRange expression can be neither a Datatype nor a DataPropertyRangeExpression:
    // if the answer is no, drop the Optional and throw an exception instead
    private static Optional<IRI> getPredicateIRI(DataRangeExpression expression) {
        if (expression instanceof Datatype) {
            return Optional.of(((Datatype) expression).getIRI());
        }
        if (expression instanceof DataPropertyRangeExpression) {
            return Optional.of(((DataPropertyRangeExpression) expression).getProperty().getIRI());
        }
        return Optional.empty();
    }

    private static class TripleObjectTypeInferenceException extends OntopInternalBugException {
        TripleObjectTypeInferenceException(IQ mappingAssertion, Variable tripleObjectVariable,
                                           String reason) {
            super("Internal bug: cannot infer the type of " + tripleObjectVariable + " in: \n" + mappingAssertion
                    + "\n Reason: " + reason);
        }
    }

    private static class UndeterminedTripleObjectTypeException extends OntopInternalBugException {
        UndeterminedTripleObjectTypeException(IRI iri, TermType tripleObjectType) {
            super("Internal bug: undetermined type (" + tripleObjectType + ") for " + iri);
        }
    }

    private static class AbstractTripleObjectTypeException extends OntopInternalBugException {
        AbstractTripleObjectTypeException(IRI iri, TermType tripleObjectType) {
            super("Internal bug: abstract type (" + tripleObjectType + ") for " + iri
                    + ". Should have been detected earlier.");
        }
    }
}
