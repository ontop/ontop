package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.input.translation.RDF4JInputQueryTranslator;
import it.unibz.inf.ontop.answering.resultset.OBDAResultSet;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.iq.IQ;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Objects;


class RDF4JInputQuery<R extends OBDAResultSet> implements InputQuery<R> {

    protected final ParsedQuery parsedQuery;
    private final String inputQueryString;

    /**
     * TODO: support bindings
     */
    RDF4JInputQuery(ParsedQuery parsedQuery, String inputQueryString) {
        this.parsedQuery = parsedQuery;
        this.inputQueryString = inputQueryString;
    }

    @Override
    public String getInputString() {
        return inputQueryString;
    }

    @Override
    public IQ translate(InputQueryTranslator translator)
            throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {
        if (!(translator instanceof RDF4JInputQueryTranslator)) {
            throw new IllegalArgumentException("RDF4JInputQueryImpl requires an RDF4JInputQueryTranslator");
        }
        return ((RDF4JInputQueryTranslator) translator).translate(parsedQuery);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RDF4JInputQuery<?> that = (RDF4JInputQuery<?>) o;
        return inputQueryString.equals(that.inputQueryString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputQueryString);
    }
}
