package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.Optional;

@FunctionalInterface
public interface FactsSupplier {

    Optional<ImmutableSet<RDFFact>> get() throws OntologyException, OWLOntologyCreationException;
}
