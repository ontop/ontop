package org.semanticweb.ontop.owlrefplatform.core.translator;

import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.ontology.OntologyVocabulary;


/**
 * Fixes the OBDA model
 */
public interface MappingVocabularyFixer {
    OBDAModel fixOBDAModel(OBDAModel model, OntologyVocabulary vocabulary,
                           NativeQueryLanguageComponentFactory nativeQLFactory);
}
