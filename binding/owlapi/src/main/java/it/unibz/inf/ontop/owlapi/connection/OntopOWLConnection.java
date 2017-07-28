package it.unibz.inf.ontop.owlapi.connection;

import it.unibz.inf.ontop.answering.connection.OntopStatement;
import org.semanticweb.owlapi.model.OWLException;

/**
 *
 * This class is mostly an OWLAPI specific wrapper for the API
 * agnostic {@link OntopStatement}.
 *
 * @see OntopStatement
 *
 */
public interface OntopOWLConnection extends OWLConnection {

    @Override
    OntopOWLStatement createStatement() throws OWLException;
}
