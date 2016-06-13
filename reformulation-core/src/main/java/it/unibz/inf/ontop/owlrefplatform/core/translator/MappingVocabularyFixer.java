package it.unibz.inf.ontop.owlrefplatform.core.translator;

import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;


/**
 * Fixes the OBDA model
 */
public interface MappingVocabularyFixer {
    OBDAModel fixOBDAModel(OBDAModel model, OntologyVocabulary vocabulary,
                           NativeQueryLanguageComponentFactory nativeQLFactory);
}
