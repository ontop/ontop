package it.unibz.inf.ontop.spec.ontology;

/*

 used only with MappingVocabularyExtractor and probably should be removed

 */

public interface OntologyVocabulary {

    OntologyVocabularyCategory<OClass> classes();

    OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties();

    OntologyVocabularyCategory<DataPropertyExpression> dataProperties();

    OntologyVocabularyCategory<AnnotationProperty> annotationProperties();

}
