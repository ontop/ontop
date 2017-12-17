package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

public interface MappingOntologyComplianceValidator {

    void validate(MappingWithProvenance preProcessedMapping, ClassifiedTBox tBox)
            throws MappingOntologyMismatchException;
}
