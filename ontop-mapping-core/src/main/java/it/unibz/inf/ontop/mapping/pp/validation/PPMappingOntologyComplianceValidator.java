package it.unibz.inf.ontop.mapping.pp.validation;

import it.unibz.inf.ontop.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public interface PPMappingOntologyComplianceValidator {

    boolean validate(PreProcessedMapping preProcessedMapping, TBoxReasoner tBox);
}
