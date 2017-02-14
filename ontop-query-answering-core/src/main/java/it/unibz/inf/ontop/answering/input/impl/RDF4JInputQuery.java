package it.unibz.inf.ontop.answering.input.impl;

import it.unibz.inf.ontop.answering.input.InputQuery;
import it.unibz.inf.ontop.answering.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.input.translation.RDF4JInputQueryTranslator;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.model.OBDAResultSet;
import it.unibz.inf.ontop.owlrefplatform.core.translator.InternalSparqlQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;


class RDF4JInputQuery<R extends OBDAResultSet> implements InputQuery<R> {

    private final ParsedQuery parsedQuery;

    /**
     * TODO: support bindings
     */
    RDF4JInputQuery(ParsedQuery parsedQuery) {
        this.parsedQuery = parsedQuery;
    }

    @Override
    public InternalSparqlQuery translate(InputQueryTranslator translator) throws OntopUnsupportedInputQueryException {
        if (!(translator instanceof RDF4JInputQueryTranslator)) {
            throw new IllegalArgumentException("RDF4JInputQueryImpl requires an RDF4JInputQueryTranslator");
        }
        return ((RDF4JInputQueryTranslator) translator).translate(parsedQuery);
    }

    protected ParsedQuery getParsedQuery() {
        return parsedQuery;
    }
}
