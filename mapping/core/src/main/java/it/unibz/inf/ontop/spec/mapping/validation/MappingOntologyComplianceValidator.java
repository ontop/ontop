package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;

public interface MappingOntologyComplianceValidator {

    void validate(MappingWithProvenance preProcessedMapping, ImmutableOntologyVocabulary signature, TBoxReasoner tBox)
            throws MappingOntologyMismatchException;
}
