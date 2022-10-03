package it.unibz.inf.ontop.query;

import it.unibz.inf.ontop.query.resultset.TupleResultSet;
import org.eclipse.rdf4j.query.BindingSet;

public interface RDF4JSelectQuery extends SelectQuery, RDF4JQuery<TupleResultSet> {

    @Override
    RDF4JSelectQuery newBindings(BindingSet bindings);
}
