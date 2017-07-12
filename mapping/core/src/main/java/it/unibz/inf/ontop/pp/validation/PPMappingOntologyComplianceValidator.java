package it.unibz.inf.ontop.pp.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pp.PreProcessedMapping;

@Deprecated
public interface PPMappingOntologyComplianceValidator {

    @Deprecated
    boolean validateMapping(PreProcessedMapping preProcessedMapping, ImmutableOntologyVocabulary signature, TBoxReasoner tBox)
            throws MappingOntologyMismatchException;
}
