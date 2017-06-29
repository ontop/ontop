package it.unibz.inf.ontop.mapping.pp.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public interface PPMappingOntologyComplianceValidator {

    boolean validateMapping(PreProcessedMapping preProcessedMapping, ImmutableOntologyVocabulary signature, TBoxReasoner tBox)
            throws MappingOntologyMismatchException;
}
