package it.unibz.inf.ontop.answering.reformulation.input;

import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import org.eclipse.rdf4j.query.BindingSet;

public interface RDF4JSelectQuery extends SelectQuery, RDF4JInputQuery<TupleResultSet> {

    @Override
    RDF4JSelectQuery newBindings(BindingSet bindings);
}
