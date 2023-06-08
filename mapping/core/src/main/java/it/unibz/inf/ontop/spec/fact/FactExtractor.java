package it.unibz.inf.ontop.spec.fact;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

import java.util.Optional;

public interface FactExtractor {

    /**
     * TODO: consider in the future additional sources
     */
    ImmutableSet<RDFFact> extractAndSelect(Optional<Ontology> ontology);

}
