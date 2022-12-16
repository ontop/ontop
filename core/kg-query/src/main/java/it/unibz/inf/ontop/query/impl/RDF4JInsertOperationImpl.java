package it.unibz.inf.ontop.query.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.query.RDF4JInsertOperation;
import it.unibz.inf.ontop.query.translation.KGQueryTranslator;
import it.unibz.inf.ontop.query.translation.RDF4JQueryTranslator;
import org.eclipse.rdf4j.query.parser.ParsedUpdate;

import java.util.Objects;

public class RDF4JInsertOperationImpl implements RDF4JInsertOperation {

    private final ParsedUpdate parsedUpdate;
    private final String queryString;

    public RDF4JInsertOperationImpl(ParsedUpdate parsedUpdate, String queryString) {
        this.queryString = queryString;
        this.parsedUpdate = parsedUpdate;
    }

    @Override
    public String getOriginalString() {
        return queryString;
    }

    @Override
    public ImmutableSet<IQ> translate(KGQueryTranslator translator) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException {
        if (!(translator instanceof RDF4JQueryTranslator)) {
            throw new IllegalArgumentException("RDF4JInsertOperationImpl requires an RDF4JInputQueryTranslator");
        }
        return ((RDF4JQueryTranslator) translator).translateInsertOperation(parsedUpdate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RDF4JInsertOperationImpl that = (RDF4JInsertOperationImpl) o;
        return queryString.equals(that.queryString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryString);
    }
}
