package it.unibz.inf.ontop.mapping.extraction.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedTriplesMap;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;

public class PPMappingOntologyComplianceValidator {

    public boolean validate(PreProcessedMapping preProcessedMapping, Ontology ontology) throws MappingOntologyMismatchException {
        return preProcessedMapping.getTripleMaps().stream().allMatch(a -> validate((PreProcessedTriplesMap) a, ontology));
    }

    private boolean validate(PreProcessedTriplesMap triplesMap, Ontology ontology) {
        return triplesMap.getTargetAtoms().stream().allMatch(f -> validate(f, ontology));
    }

    private boolean validate(Function function, Ontology ontology) {
        ImmutableOntologyVocabulary signature = ontology.getVocabulary();
//        if (isTypeAssertionTargetTriple(function)) {
//            return validateTypeAssertion(function);
//        }
//        if (isObjectPropertyTargetTriple(function)) {
//            return validateObjectPropertyAssertion(function);
//        }
//        // By default, the target triple is assumed to be a datatype Property assertion
//        return validateDatatypePropertyAssertion(function);
        return true;
    }


    private boolean isTypeAssertionTargetTriple(Function function) {
        return function.getFunctionSymbol().getName().equals(OBDAVocabulary.RDF_TYPE);
    }
}
