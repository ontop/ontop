package it.unibz.inf.ontop.rdf4j.jsonld;

import org.eclipse.rdf4j.rio.RDFHandlerException;

public class EmptyResultException extends RDFHandlerException {

    public EmptyResultException() {
        super("No results");
    }
}
