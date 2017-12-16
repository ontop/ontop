package it.unibz.inf.ontop.protege.core;

import it.unibz.inf.ontop.spec.ontology.AnnotationProperty;
import it.unibz.inf.ontop.spec.ontology.DataPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.OClass;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyExpression;

/**
 * FULLY MUTABLE Ontology Vocabulary (part of OBDAModel)
 */

public interface MutableOntologyVocabulary {

	MutableOntologyVocabularyCategory<OClass> classes();

	MutableOntologyVocabularyCategory<ObjectPropertyExpression> objectProperties();

	MutableOntologyVocabularyCategory<DataPropertyExpression> dataProperties();

	MutableOntologyVocabularyCategory<AnnotationProperty> annotationProperties();
}
