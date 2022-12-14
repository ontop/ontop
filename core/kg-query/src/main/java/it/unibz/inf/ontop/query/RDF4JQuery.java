package it.unibz.inf.ontop.query;

import it.unibz.inf.ontop.query.resultset.OBDAResultSet;
import org.eclipse.rdf4j.query.BindingSet;

public interface RDF4JQuery<R extends OBDAResultSet> extends KGQuery<R> {

    /**
     * Create a new KG query with a new binding set (replacing the previous one).
     */
    RDF4JQuery<R> newBindings(BindingSet bindings);

}
