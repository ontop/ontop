package it.unibz.inf.ontop.owlrefplatform.core.translator;

import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;


/**
 * Fixes the OBDA model
 *
 * TODO: should we generalize it other PPMapping?
 *
 */
public interface MappingVocabularyFixer {
    OBDAModel fixOBDAModel(OBDAModel model, ImmutableOntologyVocabulary vocabulary);
}
