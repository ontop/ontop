package it.unibz.inf.ontop.spec.ontology;

import com.google.common.collect.ImmutableSet;

public interface OntologyABox {

    ImmutableSet<RDFFact> getAssertions();
}
