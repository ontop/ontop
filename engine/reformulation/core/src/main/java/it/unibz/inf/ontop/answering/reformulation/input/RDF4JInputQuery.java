package it.unibz.inf.ontop.answering.reformulation.input;

import it.unibz.inf.ontop.answering.resultset.OBDAResultSet;
import org.eclipse.rdf4j.query.BindingSet;

public interface RDF4JInputQuery<R extends OBDAResultSet> extends InputQuery<R> {

    /**
     * Create a new input query with a new binding set (replacing the previous one).
     */
    RDF4JInputQuery<R> newBindings(BindingSet bindings);

}
