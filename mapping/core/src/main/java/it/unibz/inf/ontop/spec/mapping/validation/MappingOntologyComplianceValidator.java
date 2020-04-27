package it.unibz.inf.ontop.spec.mapping.validation;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.ontology.Ontology;

public interface MappingOntologyComplianceValidator {

    void validate(ImmutableList<MappingAssertion> preProcessedMapping, Ontology ontology)
            throws MappingOntologyMismatchException;
}
