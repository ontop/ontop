package it.unibz.inf.ontop.owlrefplatform.owlapi;

import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

/**
 * Ontop OWLAPI reasoner
 */
public interface OntopOWLReasoner extends OWLReasoner, AutoCloseable {

    OntopOWLConnection getConnection() throws ReasonerInternalException;

    Object getInconsistentAxiom();
}
