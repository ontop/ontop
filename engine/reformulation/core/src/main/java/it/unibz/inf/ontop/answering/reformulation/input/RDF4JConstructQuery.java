package it.unibz.inf.ontop.answering.reformulation.input;

import it.unibz.inf.ontop.answering.resultset.GraphResultSet;
import org.eclipse.rdf4j.query.BindingSet;

public interface RDF4JConstructQuery extends ConstructQuery, RDF4JInputQuery<GraphResultSet> {

    @Override
    RDF4JConstructQuery newBindings(BindingSet bindings);
}
