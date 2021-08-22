package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.input.translation.RDF4JInputQueryTranslator;
import it.unibz.inf.ontop.answering.resultset.OBDAResultSet;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.iq.IQ;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Objects;


abstract class RDF4JInputQueryImpl<R extends OBDAResultSet> implements InputQuery<R> {

    private final String inputQueryString;
    protected final BindingSet bindings;

    RDF4JInputQueryImpl(String inputQueryString, BindingSet bindings) {
        this.inputQueryString = inputQueryString;
        this.bindings = bindings;
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
        return ((RDF4JInputQueryTranslator) translator).translate(transformParsedQuery(), bindings);
    }

    protected abstract ParsedQuery transformParsedQuery() throws OntopUnsupportedInputQueryException;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RDF4JInputQueryImpl<?> that = (RDF4JInputQueryImpl<?>) o;
        return inputQueryString.equals(that.inputQueryString)
                && bindings.equals(that.bindings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputQueryString, bindings);
    }
}
