package it.unibz.inf.ontop.injection;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.Optional;

public interface OntopOntologyOWLAPIConfiguration extends OntopModelConfiguration {

    Optional<OWLOntology> loadInputOntology() throws OWLOntologyCreationException;

    Optional<ImmutableSet<RDFFact>> loadInputFacts() throws OBDASpecificationException;

    /**
     * Only call it if you are sure that an ontology has been provided
     */
    default OWLOntology loadProvidedInputOntology() throws OWLOntologyCreationException {
        return loadInputOntology()
                .orElseThrow(() -> new IllegalStateException("No ontology has been provided. " +
                        "Do not call this method unless you are sure of the ontology provision."));
    }
}
