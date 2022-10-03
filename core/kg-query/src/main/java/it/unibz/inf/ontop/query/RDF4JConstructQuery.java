package it.unibz.inf.ontop.query;

import it.unibz.inf.ontop.query.resultset.GraphResultSet;
import org.eclipse.rdf4j.query.BindingSet;

public interface RDF4JConstructQuery extends ConstructQuery, RDF4JInputQuery<GraphResultSet> {

    @Override
    RDF4JConstructQuery newBindings(BindingSet bindings);
}
