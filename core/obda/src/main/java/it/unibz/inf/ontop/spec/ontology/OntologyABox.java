package it.unibz.inf.ontop.spec.ontology;

import com.google.common.collect.ImmutableList;

public interface OntologyABox {

    ImmutableList<ClassAssertion> getClassAssertions();

    ImmutableList<ObjectPropertyAssertion> getObjectPropertyAssertions();

    ImmutableList<DataPropertyAssertion> getDataPropertyAssertions();

    ImmutableList<AnnotationAssertion> getAnnotationAssertions();
}
