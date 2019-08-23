package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.ontology.Ontology;

public interface MappingOntologyComplianceValidator {

    void validate(MappingWithProvenance preProcessedMapping, Ontology ontology)
            throws MappingOntologyMismatchException;
}
