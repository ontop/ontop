package it.unibz.inf.ontop.rdf4j.predefined.parsing;

import org.eclipse.rdf4j.RDF4JException;

/**
 * TODO: better think about it
 */
public class PredefinedQueryConfigException extends RDF4JException {

    public PredefinedQueryConfigException(String msg) {
        super(msg);
    }

    public PredefinedQueryConfigException(Throwable t) {
        super(t);
    }

    public PredefinedQueryConfigException(String msg, Throwable t) {
        super(msg, t);
    }


}
