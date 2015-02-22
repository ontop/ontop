package org.semanticweb.ontop.owlrefplatform.core.translator;

import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.OntologyVocabulary;

import java.util.Set;

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
    public OBDAModel fixOBDAModel(OBDAModel model, OntologyVocabulary vocabulary) {
        return model;
    }
}
