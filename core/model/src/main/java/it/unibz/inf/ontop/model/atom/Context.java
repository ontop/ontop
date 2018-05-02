package it.unibz.inf.ontop.model.atom;

import org.apache.commons.rdf.api.IRI;

/**
 * Context generalizes the notion of named graphs.
 *
 * It can be extended with domain-specific concerns
 *  (e.g. beginning and end of intervals in a temporal extension).
 */
public interface Context {

    IRI getGraphIRI();
}
