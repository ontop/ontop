package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.AskQuery;
import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.input.translation.RDF4JInputQueryTranslator;
import it.unibz.inf.ontop.answering.resultset.BooleanResultSet;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.iq.IQ;
import org.eclipse.rdf4j.query.parser.ParsedQuery;


class RDF4JAskQuery extends RDF4JInputQuery<BooleanResultSet> implements AskQuery {

    RDF4JAskQuery(ParsedQuery parsedQuery, String queryString) {
        super(parsedQuery, queryString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RDF4JAskQuery that = (RDF4JAskQuery) o;

        return getParsedQuery().equals(that.getParsedQuery());
    }

    @Override
    public int hashCode() {
        return getParsedQuery().hashCode();
    }

    @Override
    public IQ translate(InputQueryTranslator translator) throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {
        if (!(translator instanceof RDF4JInputQueryTranslator)) {
            throw new IllegalArgumentException("RDF4JInputQueryImpl requires an RDF4JInputQueryTranslator");
        }
        return ((RDF4JInputQueryTranslator) translator).translateAskQuery(parsedQuery);
    }
}
