package it.unibz.inf.ontop.mapping.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public interface MappingOntologyComplianceValidator {

    boolean validate(MappingWithProvenance preProcessedMapping, ImmutableOntologyVocabulary signature, TBoxReasoner tBox)
            throws MappingOntologyMismatchException;
}
