package it.unibz.inf.ontop.mapping.extraction.validation;

import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.ontology.Ontology;

public interface PPMappingOntologyComplianceValidator {

    boolean validate(PreProcessedMapping preProcessedMapping, Ontology ontology);
}
