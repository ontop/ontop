package it.unibz.inf.ontop.owlrefplatform.core.translator;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.SQLPPMappingAxiom;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;


@Deprecated
public interface MappingVocabularyFixer {
    ImmutableList<SQLPPMappingAxiom> fixMappingAxioms(ImmutableList<SQLPPMappingAxiom> mappingAxioms,
                                                      ImmutableOntologyVocabulary vocabulary);
}
