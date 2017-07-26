package it.unibz.inf.ontop.owlrefplatform.owlapi;

import it.unibz.inf.ontop.owlrefplatform.core.OntopStatement;
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
