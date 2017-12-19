package it.unibz.inf.ontop.spec.ontology.impl;

import it.unibz.inf.ontop.spec.ontology.*;

public class OntologyVocabularyImpl implements OntologyVocabulary {
    private final OntologyImpl.ImmutableOntologyVocabularyCategoryImpl<OClass> classes;
    private final OntologyImpl.ImmutableOntologyVocabularyCategoryImpl<ObjectPropertyExpression> objectProperties;
    private final OntologyImpl.ImmutableOntologyVocabularyCategoryImpl<DataPropertyExpression> dataProperties;
    private final OntologyImpl.ImmutableOntologyVocabularyCategoryImpl<AnnotationProperty> annotationProperties;


    OntologyVocabularyImpl(OntologyImpl.ImmutableOntologyVocabularyCategoryImpl<OClass> classes,
                           OntologyImpl.ImmutableOntologyVocabularyCategoryImpl<ObjectPropertyExpression> objectProperties,
                           OntologyImpl.ImmutableOntologyVocabularyCategoryImpl<DataPropertyExpression> dataProperties,
                           OntologyImpl.ImmutableOntologyVocabularyCategoryImpl<AnnotationProperty> annotationProperties) {
        this.classes = classes;
        this.objectProperties = objectProperties;
        this.dataProperties = dataProperties;
        this.annotationProperties = annotationProperties;
    }

    @Override
    public OntologyVocabularyCategory<OClass> classes() { return classes; }

    @Override
    public OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties() { return objectProperties; }

    @Override
    public OntologyVocabularyCategory<DataPropertyExpression> dataProperties() { return dataProperties; }

    @Override
    public OntologyVocabularyCategory<AnnotationProperty> annotationProperties() { return annotationProperties; }
}
