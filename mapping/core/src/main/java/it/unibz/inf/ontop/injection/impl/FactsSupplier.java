package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

import java.util.Optional;

@FunctionalInterface
public interface FactsSupplier {

    Optional<ImmutableSet<RDFFact>> get() throws OBDASpecificationException;
}
