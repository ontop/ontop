package it.unibz.inf.ontop.answering.reformulation.input;

import org.eclipse.rdf4j.query.BindingSet;

import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;

public interface RDF4JConstructQuery extends ConstructQuery, RDF4JInputQuery<SimpleGraphResultSet> {

    @Override
    RDF4JConstructQuery newBindings(BindingSet bindings);
}
