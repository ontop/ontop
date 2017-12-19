package it.unibz.inf.ontop.spec.ontology;

public interface OntologyVocabulary {

    OntologyVocabularyCategory<OClass> classes();

    OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties();

    OntologyVocabularyCategory<DataPropertyExpression> dataProperties();

    OntologyVocabularyCategory<AnnotationProperty> annotationProperties();

}
