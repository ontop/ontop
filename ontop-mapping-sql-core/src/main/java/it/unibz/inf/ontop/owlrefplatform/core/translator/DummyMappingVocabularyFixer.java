package it.unibz.inf.ontop.owlrefplatform.core.translator;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.SQLPPMappingAxiom;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;

/**
 * Fixes nothing.
 *
 */
@Deprecated
public class DummyMappingVocabularyFixer implements MappingVocabularyFixer {
    /**
     * Returns the same model (fixes nothing).
     *
     */
    @Override
    public ImmutableList<SQLPPMappingAxiom> fixMappingAxioms(ImmutableList<SQLPPMappingAxiom> mappingAxioms,
                                                             ImmutableOntologyVocabulary vocabulary) {
        return mappingAxioms;
    }
}
