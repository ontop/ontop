package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

/**
 * Ontop OWLAPI reasoner
 */
public interface OntopOWLReasoner extends OWLReasoner, AutoCloseable {

    OntopOWLConnection getConnection() throws ReasonerInternalException;

    Object getInconsistentAxiom();
}
