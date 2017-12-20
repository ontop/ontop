package it.unibz.inf.ontop.protege.core;

/**
 * FULLY MUTABLE Ontology Vocabulary (part of OBDAModel)
 */

public interface MutableOntologyVocabulary {

	MutableOntologyVocabularyCategory classes();

	MutableOntologyVocabularyCategory objectProperties();

	MutableOntologyVocabularyCategory dataProperties();

	MutableOntologyVocabularyCategory annotationProperties();
}
