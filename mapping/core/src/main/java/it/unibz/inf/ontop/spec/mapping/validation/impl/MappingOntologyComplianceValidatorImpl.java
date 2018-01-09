package it.unibz.inf.ontop.spec.mapping.validation.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.Equivalences;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;


@Singleton
public class MappingOntologyComplianceValidatorImpl implements MappingOntologyComplianceValidator {

    private static final String DATA_PROPERTY_STR = "a data property";
    private static final String OBJECT_PROPERTY_STR = "an object property";
    private static final String ANNOTATION_PROPERTY_STR = "an annotation property";
    private static final String CLASS_STR = "a class";
    private final TermFactory termFactory;
    private final AtomFactory atomFactory;
    private final TypeFactory typeFactory;

    @Inject
    private MappingOntologyComplianceValidatorImpl(TermFactory termFactory, AtomFactory atomFactory, TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.atomFactory = atomFactory;
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
    public void validate(MappingWithProvenance mapping, Ontology ontology)
            throws MappingOntologyMismatchException {

        ImmutableMultimap<String, Datatype> datatypeMap = computeDataTypeMap(ontology.tbox());

        for (Map.Entry<IntermediateQuery, PPMappingAssertionProvenance> entry : mapping.getProvenanceMap().entrySet()) {
            validateAssertion(entry.getKey(), entry.getValue(), ontology, datatypeMap);
        }
    }

    private void validateAssertion(IntermediateQuery mappingAssertion, PPMappingAssertionProvenance provenance,
                                   Ontology ontology,
                                   ImmutableMultimap<String, Datatype> datatypeMap)
            throws MappingOntologyMismatchException {

        String predicateIRI = extractPredicateIRI(mappingAssertion);

        Optional<RDFTermType> tripleObjectType = extractTripleObjectType(mappingAssertion);
        checkTripleObject(predicateIRI, tripleObjectType, provenance, ontology, datatypeMap);
    }

    private String extractPredicateIRI(IntermediateQuery mappingAssertion) {
        AtomPredicate projectionAtomPredicate = mappingAssertion.getProjectionAtom().getPredicate();
        if (projectionAtomPredicate.equals(atomFactory.getTripleAtomPredicate()))
            throw new RuntimeException("TODO: extract the RDF predicate from a triple atom");
        else
            return projectionAtomPredicate.getName();
    }

    /**
     * For a mapping assertion using an RDF property (not rdf:type) the building expression of the triple object
     * variable is assumed to be present in the ROOT construction node.
     *
     * Note that this assumption does not hold for intermediate query in general.
     *
     * Return nothing if the property is rdf:type (class instance assertion)
     *
     * TODO: refactor it!
     *
     */
    private Optional<RDFTermType> extractTripleObjectType(IntermediateQuery mappingAssertion)
            throws TripleObjectTypeInferenceException {

        Optional<Variable> optionalObjectVariable = extractTripleObjectVariable(mappingAssertion);
        if (optionalObjectVariable.isPresent()) {
            Variable objectVariable = optionalObjectVariable.get();

            ImmutableTerm constructionTerm = Optional.of(mappingAssertion.getRootNode())
                    .filter(n -> n instanceof ConstructionNode)
                    .map((n) -> (ConstructionNode) n)
                    .map(ConstructionNode::getSubstitution)
                    .flatMap(s -> Optional.ofNullable(s.get(objectVariable)))
                    .orElseThrow(() -> new TripleObjectTypeInferenceException(mappingAssertion, objectVariable,
                            "Not defined in the root node (expected for a mapping assertion)"));

            if (constructionTerm instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm constructionFunctionalTerm = ((ImmutableFunctionalTerm) constructionTerm);
                Predicate functionSymbol = constructionFunctionalTerm.getFunctionSymbol();
                if ((functionSymbol instanceof BNodePredicate)
                        || (functionSymbol instanceof URITemplatePredicate)) {
                    return Optional.of(typeFactory.getIRITermType());
                }
                else if (functionSymbol instanceof DatatypePredicate) {
                    DatatypePredicate datatypeConstructionFunctionSymbol = (DatatypePredicate) functionSymbol;
                    return Optional.of(datatypeConstructionFunctionSymbol.getReturnedType());
                }
                else {
                    throw new TripleObjectTypeInferenceException(mappingAssertion, objectVariable,
                            "Unexpected function symbol: " + functionSymbol);
                }
            }

            else {
                /*
                 * TODO: consider variables and constants (NB: could be relevant for SPARQL->SPARQL
                  * but not much for SPARQL->SQL where RDF terms have to built)
                 */
                throw new TripleObjectTypeInferenceException(mappingAssertion, objectVariable,
                        "Was expecting a functional term (constants and variables are not yet supported). \n"
                                + "Term definition: " + constructionTerm);
            }

        }
        /*
         * Class property
         */
        else
            return Optional.empty();
    }

    private RDFDatatype extractLangTermType(ImmutableFunctionalTerm constructionFunctionalTerm) {
        ImmutableList<? extends ImmutableTerm> arguments = constructionFunctionalTerm.getArguments();
        ImmutableTerm langTerm = arguments.get(1);

        if (!(langTerm instanceof Constant)) {
            // TODO: throw a proper exception
            throw new IllegalStateException("A langString must have a constant language tag: "
                    + constructionFunctionalTerm);
        }
        return typeFactory.getLangTermType(((Constant) langTerm).getValue());
    }

    private Optional<Variable> extractTripleObjectVariable(IntermediateQuery mappingAssertion)
            throws TripleObjectTypeInferenceException {
        ImmutableList<Variable> projectedVariables = mappingAssertion.getProjectionAtom().getArguments();

        switch (projectedVariables.size()) {
            // Class
            case 1:
                return Optional.empty();
            // Property
            case 2:
                return Optional.of(projectedVariables.get(1));
            // Triple predicate
            case 3:
                return Optional.of(projectedVariables.get(2));
            default:
                throw new TripleObjectTypeInferenceException(mappingAssertion, "Unexpected arity of the projection atom");
        }
    }



    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void checkTripleObject(String predicateIRI, Optional<RDFTermType> optionalTripleObjectType,
                                   PPMappingAssertionProvenance provenance,
                                   Ontology ontology,
                                   ImmutableMultimap<String, Datatype> datatypeMap)
            throws MappingOntologyMismatchException {

        if (optionalTripleObjectType.isPresent()) {
            RDFTermType tripleObjectType = optionalTripleObjectType.get();

            if (tripleObjectType.isAbstract())
                throw new AbstractTripleObjectType(predicateIRI, tripleObjectType);

            /*
             * TODO: avoid instanceof tests!
             */
            if (tripleObjectType instanceof ObjectRDFType) {
                checkObjectOrAnnotationProperty(predicateIRI, provenance, ontology);
                return;
            }
            else if (tripleObjectType instanceof RDFDatatype) {
                checkDataOrAnnotationProperty((RDFDatatype)tripleObjectType, predicateIRI, provenance, ontology,
                        datatypeMap);
            }
            else {
                // E.g. Unbound
                throw new UndeterminedTripleObjectType(predicateIRI, tripleObjectType);
            }
        }
        else {
            checkClass(predicateIRI, provenance, ontology);
        }
    }

    private void checkObjectOrAnnotationProperty(String predicateIRI, PPMappingAssertionProvenance provenance,
                                                 Ontology ontology)
            throws MappingOntologyMismatchException {
        /*
         * Cannot be a data property (should be either an object or an annotation property)
         */
        if (ontology.tbox().dataProperties().contains(predicateIRI))
            throw new MappingOntologyMismatchException(generatePropertyOrClassConflictMessage(predicateIRI, provenance,
                    DATA_PROPERTY_STR, OBJECT_PROPERTY_STR));
        /*
         * Cannot be a class
         */
        if (ontology.tbox().classes().contains(predicateIRI))
            throw new MappingOntologyMismatchException(generatePropertyOrClassConflictMessage(predicateIRI, provenance,
                    CLASS_STR, OBJECT_PROPERTY_STR));
    }

    private void checkDataOrAnnotationProperty(RDFDatatype tripleObjectType, String predicateIRI,
                                               PPMappingAssertionProvenance provenance,
                                               Ontology ontology,
                                               ImmutableMultimap<String, Datatype> datatypeMap)
            throws MappingOntologyMismatchException {
        /*
         * Cannot be an object property
         */
        if (ontology.tbox().objectProperties().contains(predicateIRI))
            throw new MappingOntologyMismatchException(generatePropertyOrClassConflictMessage(predicateIRI, provenance,
                    OBJECT_PROPERTY_STR, DATA_PROPERTY_STR));
        /*
         * Cannot be a class
         */
        if (ontology.tbox().classes().contains(predicateIRI))
            throw new MappingOntologyMismatchException(generatePropertyOrClassConflictMessage(predicateIRI, provenance,
                    CLASS_STR, DATA_PROPERTY_STR));

        /*
         * Checks the datatypes
         */
        for (Datatype declaredDatatype : datatypeMap.get(predicateIRI)) {

            if(declaredDatatype.getIRI().equals(RDFS.LITERAL)){
                break;
            }
            // TODO: throw a better exception
            RDFDatatype declaredTermType = typeFactory.getOptionalDatatype(declaredDatatype.getIRI())
                    .orElseThrow(() -> new RuntimeException("Unsupported datatype declared in the ontology: "
                            + declaredDatatype + " used for " + predicateIRI));

            if (!tripleObjectType.isA(declaredTermType)) {

                throw new MappingOntologyMismatchException(
                        predicateIRI +
                                " is declared with datatype " +
                                declaredDatatype +
                                " in the ontology, but has datatype " +
                                termFactory.getRequiredTypePredicate(tripleObjectType).getName() +
                                " according to the following triplesMap (either declared in the triplesMap, or " +
                                "inferred from its source):\n[\n" +
                                provenance.getProvenanceInfo() +
                                "\n]\n"
                );
            }
        }
    }

    private void checkClass(String predicateIRI, PPMappingAssertionProvenance provenance,
                            Ontology ontology) throws MappingOntologyMismatchException {
        /*
         * Cannot be an object property
         */
        if (ontology.tbox().objectProperties().contains(predicateIRI))
            throw new MappingOntologyMismatchException(generatePropertyOrClassConflictMessage(predicateIRI, provenance,
                    OBJECT_PROPERTY_STR, CLASS_STR));
        /*
         * Cannot be a data property
         */
        else if (ontology.tbox().dataProperties().contains(predicateIRI))
            throw new MappingOntologyMismatchException(generatePropertyOrClassConflictMessage(predicateIRI, provenance,
                    DATA_PROPERTY_STR, CLASS_STR));

        /*
         * Cannot be an annotation property
         */
        if (ontology.annotationProperties().contains(predicateIRI))
            throw new MappingOntologyMismatchException(generatePropertyOrClassConflictMessage(predicateIRI, provenance,
                    ANNOTATION_PROPERTY_STR, DATA_PROPERTY_STR));
    }

    private static String generatePropertyOrClassConflictMessage(String predicateIRI, PPMappingAssertionProvenance provenance,
                                                                 String declaredTypeString, String usedTypeString) {

        return predicateIRI +
                " is declared as " +
                declaredTypeString +
                " in the ontology, but is used as " +
                usedTypeString +
                " in the triplesMap: \n[\n" +
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
    private ImmutableMultimap<String, Datatype> computeDataTypeMap(ClassifiedTBox reasoner) {
        // TODO: switch to guava > 2.1, and replace by Streams.stream(iterable)
        return StreamSupport.stream(reasoner.dataRangesDAG().spliterator(), false)
                .flatMap(n -> getPartialPredicateToDatatypeMap(n, reasoner).entrySet().stream())
                .collect(ImmutableCollectors.toMultimap(
                        e -> e.getKey().getName(),
                        Map.Entry::getValue));
    }


    private ImmutableMap<Predicate, Datatype> getPartialPredicateToDatatypeMap(Equivalences<DataRangeExpression> nodeSet,
                                                                               ClassifiedTBox reasoner) {
        DataRangeExpression node = nodeSet.getRepresentative();

        return ImmutableMap.<Predicate, Datatype>builder()
                .putAll(getDescendentNodesPartialMap(reasoner, node, nodeSet))
                .putAll(getEquivalentNodesPartialMap(node, nodeSet))
                .build();
    }

    private ImmutableMap<Predicate, Datatype> getDescendentNodesPartialMap(ClassifiedTBox reasoner, DataRangeExpression node,
                                                                           Equivalences<DataRangeExpression> nodeSet) {
        if (node instanceof Datatype) {
            return reasoner.dataRangesDAG().getSub(nodeSet).stream()
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

    private ImmutableMap<Predicate, Datatype> getEquivalentNodesPartialMap(DataRangeExpression node,
                                                                           Equivalences<DataRangeExpression> nodeSet) {
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
            return typeFactory.getOptionalDatatype(((Datatype) expression).getIRI())
                    .flatMap(termFactory::getOptionalTypePredicate)
                    .map(p -> (Predicate) p);
        }
        if (expression instanceof DataPropertyRangeExpression) {
            return Optional.of(atomFactory.getDataPropertyPredicate(
                    ((DataPropertyRangeExpression) expression).getProperty().getIRI()));
        }
        return Optional.empty();
    }

    private static class TripleObjectTypeInferenceException extends OntopInternalBugException {
        TripleObjectTypeInferenceException(IntermediateQuery mappingAssertion, Variable tripleObjectVariable,
                                           String reason) {
            super("Internal bug: cannot infer the type of " + tripleObjectVariable + " in: \n" + mappingAssertion
                    + "\n Reason: " + reason);
        }

        TripleObjectTypeInferenceException(IntermediateQuery mappingAssertion, String reason) {
            super("Internal bug: cannot infer the type of the object term " + " in: \n" + mappingAssertion
                    + "\n Reason: " + reason);
        }
    }

    private static class UndeterminedTripleObjectType extends OntopInternalBugException {
        UndeterminedTripleObjectType(String predicateName, TermType tripleObjectType) {
            super("Internal bug: undetermined type (" + tripleObjectType + ") for " + predicateName);
        }
    }

    private static class AbstractTripleObjectType extends OntopInternalBugException {
        AbstractTripleObjectType(String predicateName, TermType tripleObjectType) {
            super("Internal bug: abstract type (" + tripleObjectType + ") for " + predicateName
                    + ". Should have been detected earlier.");
        }
    }

    private Variable generateFreshVariable() {
        return termFactory.getVariable("fresh-" + UUID.randomUUID());
    }
}
