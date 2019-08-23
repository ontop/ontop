package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.spec.ontology.Ontology;

import java.util.Optional;

@FunctionalInterface
public interface OntologySupplier {

    Optional<Ontology> get() throws OntologyException;
}
