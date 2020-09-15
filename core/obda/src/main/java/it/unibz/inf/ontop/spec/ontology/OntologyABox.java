package it.unibz.inf.ontop.spec.ontology;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public interface OntologyABox {

    @Deprecated
    ImmutableList<ClassAssertion> getClassAssertions();

    @Deprecated
    ImmutableList<ObjectPropertyAssertion> getObjectPropertyAssertions();

    @Deprecated
    ImmutableList<DataPropertyAssertion> getDataPropertyAssertions();

    @Deprecated
    ImmutableList<AnnotationAssertion> getAnnotationAssertions();

    ImmutableSet<RDFFact> getAssertions();
}
