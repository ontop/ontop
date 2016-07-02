package it.unibz.inf.ontop.owlrefplatform.core.translator;

import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;

/**
 * Fixes nothing.
 *
 */
public class DummyMappingVocabularyFixer implements MappingVocabularyFixer {
    /**
     * Returns the same model (fixes nothing).
     *
     */
    @Override
    public OBDAModel fixOBDAModel(OBDAModel model, ImmutableOntologyVocabulary vocabulary,
                                  NativeQueryLanguageComponentFactory nativeQLFactory) {
        return model;
    }
}
