package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;


/**
 * Not for production! Use RDF4J instead.
 *
 * OWL just in the name but actually independpent from OWLOntology files
 *
 */
public interface OntopOWLEngine extends AutoCloseable {

    OntopOWLConnection getConnection() throws ReasonerInternalException;
}
