package it.unibz.inf.ontop.mapping.extraction.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedTriplesMap;
import it.unibz.inf.ontop.model.DatatypePredicate;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;

import java.util.Optional;

public class PPMappingOntologyComplianceValidator {

    private static enum ErrorType {
        CLASS_DATATYPEPROPERTY,
        CLASS_OBJECTPROPERTY,
        CLASS_ANNOTATIONPROPERTY,
//        DATATYPEPROPERTY_OBJECTPROPERTY,
//        DATATYPEPROPERTY_OBJECTPROPERTY,
//        DATATYPEPROPERTY_OBJECTPROPERTY,
//        DATATYPEPROPERTY_OBJECTPROPERTY,
        DATATYPEPROPERTY_OBJECTPROPERTY


    };

    public static boolean validate(PreProcessedMapping preProcessedMapping, Ontology ontology) throws
            MappingOntologyMismatchException {
        return  preProcessedMapping.getTripleMaps().stream()
                .allMatch(t -> validate((PreProcessedTriplesMap)t, ontology.getVocabulary()));
    }

    private static boolean validate(PreProcessedTriplesMap triplesMap, ImmutableOntologyVocabulary
            ontologySignature) {
        return triplesMap.getTargetAtoms().stream()
                .allMatch(f -> validate(triplesMap, f, ontologySignature));
    }

    private static boolean validate(PreProcessedTriplesMap triplesMap, ImmutableFunctionalTerm targetTriple, ImmutableOntologyVocabulary ontologySignature) throws MappingOntologyMismatchException {
        if (isClassAssertion(targetTriple)) {
            return validateClassAssertion(targetTriple, triplesMap, ontologySignature);
        }
        if (isDataAssertion(targetTriple)) {
            return validateDataAssertion(targetTriple, triplesMap, ontologySignature);
        }
        if (isObjectAssertion(targetTriple)) {
            return validateObjectAssertion(targetTriple, triplesMap, ontologySignature);
        }
        return true;
    }

    private static boolean validateObjectAssertion(ImmutableFunctionalTerm targetTriple, PreProcessedTriplesMap triplesMap, ImmutableOntologyVocabulary ontologySignature) {
        return false;
    }

    private static boolean isObjectAssertion(ImmutableFunctionalTerm targetTriple) {
        return targetTriple.getArity() == 2 &&
                targetTriple.getTerm(2) instanceof ImmutableFunctionalTerm &&
                ((ImmutableFunctionalTerm) targetTriple.getTerm(2)).getFunctionSymbol() instanceof DatatypePredicate;
    }

    private static boolean validateDataAssertion(ImmutableFunctionalTerm targetTriple, PreProcessedTriplesMap triplesMap, ImmutableOntologyVocabulary ontologySignature) {
        String predicateString = targetTriple.getFunctionSymbol().getName();
        Optional<String> errorMessage = Optional.empty();

        if(ontologySignature.containsObjectProperty(predicateString)){
            errorMessage = Optional.of(generateMisMatchMessage(targetTriple, triplesMap, ErrorType.DATATYPEPROPERTY_OBJECTPROPERTY));
        }
        if(errorMessage.isPresent()){
            throw new MappingOntologyMismatchException(errorMessage.get());
        }
        return true;
    }

    private static boolean isDataAssertion(ImmutableFunctionalTerm targetTriple) {
        return targetTriple.getArity() == 2 &&
                targetTriple.getTerm(2) instanceof ImmutableFunctionalTerm &&
                ((ImmutableFunctionalTerm) targetTriple.getTerm(2)).getFunctionSymbol() instanceof DatatypePredicate;
    }

    private static boolean validateClassAssertion(ImmutableFunctionalTerm targetTriple, PreProcessedTriplesMap triplesMap,
                                                  ImmutableOntologyVocabulary ontologySignature) throws MappingOntologyMismatchException {
        String className = targetTriple.getFunctionSymbol().getName();
        Optional<String> errorMessage = Optional.empty();

        if(ontologySignature.containsDataProperty(className)){
            errorMessage = Optional.of(generateMisMatchMessage(targetTriple, triplesMap, ErrorType.CLASS_DATATYPEPROPERTY));
        }
        if(ontologySignature.containsAnnotationProperty(className)){
            errorMessage = Optional.of(generateMisMatchMessage(targetTriple, triplesMap, ErrorType.CLASS_ANNOTATIONPROPERTY));
        }
        if(ontologySignature.containsObjectProperty(className)){
            errorMessage = Optional.of(generateMisMatchMessage(targetTriple, triplesMap, ErrorType.CLASS_OBJECTPROPERTY));
        }
        if(errorMessage.isPresent()){
            throw new MappingOntologyMismatchException(errorMessage.get());
        }
        return true;
    }

    private static String generateMisMatchMessage(ImmutableFunctionalTerm targetTriple, PreProcessedTriplesMap
            triplesMap, ErrorType errorType) {

        String typeInMapping;
        String typeInOntology;


        String message ="";


        switch (errorType) {
            case CLASS_DATATYPEPROPERTY:
            case CLASS_OBJECTPROPERTY:
            case CLASS_ANNOTATIONPROPERTY:
                typeInMapping = "a class";
                break;
            default:
                throw new IllegalArgumentException("Unexpected mismatch");
        }

        switch (errorType) {
            case CLASS_DATATYPEPROPERTY:
                typeInOntology = "a Datatype property";
                break;
            case CLASS_OBJECTPROPERTY:
                typeInOntology = "an Object property";
                break;
            case CLASS_ANNOTATIONPROPERTY:
                 typeInOntology = "an Annotation property";
                break;
            default:
                throw new IllegalArgumentException("Unexpected mismatch");
        }



        message = targetTriple.getFunctionSymbol().getName()+
                " is used both as "+
                typeInOntology+
                " in the ontology, and as "+
                typeInMapping+
                " in target atom "+
                targetTriple+
                " of the triplesMap: \n[\n"+
                triplesMap.getProvenance(targetTriple).getProvenanceInfo()+
                "\n]";

        System.out.println(message);
        return message;
    }


    private static boolean isClassAssertion(Function function) {
        return function.getArity() == 1;
    }
}
