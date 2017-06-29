package it.unibz.inf.ontop.owlrefplatform.core.translator;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.SQLPPTriplesMap;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;


@Deprecated
public interface MappingVocabularyFixer {
    ImmutableList<SQLPPTriplesMap> fixMappingAxioms(ImmutableList<SQLPPTriplesMap> mappingAxioms,
                                                    ImmutableOntologyVocabulary vocabulary);
}
