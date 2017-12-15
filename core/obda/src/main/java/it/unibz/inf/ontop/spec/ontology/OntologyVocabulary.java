package it.unibz.inf.ontop.spec.ontology;

/**
 * MUTABLE Ontology Vocabulary
 */

public interface OntologyVocabulary {

	OntologyVocabularyCategory<OClass> classes();

	OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties();

	OntologyVocabularyCategory<DataPropertyExpression> dataProperties();

	OntologyVocabularyCategory<AnnotationProperty> annotationProperties();

	Datatype getDatatype(String uri);

	void merge(Ontology ontology);
}
