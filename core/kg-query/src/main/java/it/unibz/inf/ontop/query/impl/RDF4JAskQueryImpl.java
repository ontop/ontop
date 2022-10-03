package it.unibz.inf.ontop.query.impl;

import it.unibz.inf.ontop.query.RDF4JAskQuery;
import it.unibz.inf.ontop.query.RDF4JQuery;
import it.unibz.inf.ontop.query.translation.InputQueryTranslator;
import it.unibz.inf.ontop.query.translation.RDF4JInputQueryTranslator;
import it.unibz.inf.ontop.query.resultset.BooleanResultSet;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.iq.IQ;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;


class RDF4JAskQueryImpl extends RegularRDF4JKGQueryImpl<BooleanResultSet> implements RDF4JAskQuery {

    RDF4JAskQueryImpl(ParsedQuery parsedQuery, String queryString, BindingSet bindings) {
        super(parsedQuery, queryString, bindings);
    }

    @Override
    public IQ translate(InputQueryTranslator translator) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException {
        if (!(translator instanceof RDF4JInputQueryTranslator)) {
            throw new IllegalArgumentException("RDF4JInputQueryImpl requires an RDF4JInputQueryTranslator");
        }
        return ((RDF4JInputQueryTranslator) translator).translateAskQuery(parsedQuery, bindings);
    }

    @Override
    public RDF4JQuery<BooleanResultSet> newBindings(BindingSet newBindings) {
        return new RDF4JAskQueryImpl(parsedQuery, getOriginalString(), newBindings);
    }
}
