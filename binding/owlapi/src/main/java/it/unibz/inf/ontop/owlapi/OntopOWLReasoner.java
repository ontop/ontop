package it.unibz.inf.ontop.owlapi;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Ontop OWLAPI reasoner
 */
public interface OntopOWLReasoner extends OWLReasoner, OntopOWLEngine {

    Object getInconsistentAxiom();
}
