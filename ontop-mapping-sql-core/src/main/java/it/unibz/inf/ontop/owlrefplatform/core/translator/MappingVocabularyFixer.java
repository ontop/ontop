package it.unibz.inf.ontop.owlrefplatform.core.translator;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;


/**
 * Fixes the relational mapping axioms
 *
 * TODO: should we generalize it other mapping axioms?
 *
 */
public interface MappingVocabularyFixer {
    ImmutableList<OBDAMappingAxiom> fixMappingAxioms(ImmutableList<OBDAMappingAxiom> mappingAxioms,
                                                     ImmutableOntologyVocabulary vocabulary);
}
