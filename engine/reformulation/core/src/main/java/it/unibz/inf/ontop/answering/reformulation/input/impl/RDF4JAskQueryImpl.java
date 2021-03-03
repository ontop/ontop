package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.RDF4JAskQuery;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQuery;
import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.input.translation.RDF4JInputQueryTranslator;
import it.unibz.inf.ontop.answering.resultset.BooleanResultSet;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.iq.IQ;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;


class RDF4JAskQueryImpl extends RegularRDF4JInputImpl<BooleanResultSet> implements RDF4JAskQuery {

    RDF4JAskQueryImpl(ParsedQuery parsedQuery, String queryString, BindingSet bindings) {
        super(parsedQuery, queryString, bindings);
    }

    @Override
    public IQ translate(InputQueryTranslator translator) throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {
        if (!(translator instanceof RDF4JInputQueryTranslator)) {
            throw new IllegalArgumentException("RDF4JInputQueryImpl requires an RDF4JInputQueryTranslator");
        }
        return ((RDF4JInputQueryTranslator) translator).translateAskQuery(parsedQuery, bindings);
    }

    @Override
    public RDF4JInputQuery<BooleanResultSet> newBindings(BindingSet newBindings) {
        return new RDF4JAskQueryImpl(parsedQuery, getInputString(), newBindings);
    }
}
