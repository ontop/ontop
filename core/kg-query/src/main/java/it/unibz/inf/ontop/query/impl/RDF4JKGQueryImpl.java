package it.unibz.inf.ontop.query.impl;

import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.query.translation.KGQueryTranslator;
import it.unibz.inf.ontop.query.translation.RDF4JQueryTranslator;
import it.unibz.inf.ontop.query.resultset.OBDAResultSet;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.iq.IQ;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Objects;


abstract class RDF4JKGQueryImpl<R extends OBDAResultSet> implements KGQuery<R> {

    private final String inputQueryString;
    protected final BindingSet bindings;

    RDF4JKGQueryImpl(String inputQueryString, BindingSet bindings) {
        this.inputQueryString = inputQueryString;
        this.bindings = bindings;
    }

    @Override
    public String getOriginalString() {
        return inputQueryString;
    }

    @Override
    public IQ translate(KGQueryTranslator translator)
            throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException {
        if (!(translator instanceof RDF4JQueryTranslator)) {
            throw new IllegalArgumentException("RDF4JInputQueryImpl requires an RDF4JInputQueryTranslator");
        }
        return ((RDF4JQueryTranslator) translator).translateQuery(transformParsedQuery(), bindings);
    }

    protected abstract ParsedQuery transformParsedQuery() throws OntopUnsupportedKGQueryException;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RDF4JKGQueryImpl<?> that = (RDF4JKGQueryImpl<?>) o;
        return inputQueryString.equals(that.inputQueryString)
                && bindings.equals(that.bindings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputQueryString, bindings);
    }
}
