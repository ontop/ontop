package it.unibz.inf.ontop.rdf4j.predefined.impl;

import it.unibz.inf.ontop.rdf4j.predefined.PredefinedQuery;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Optional;

public class AbstractPredefinedQuery implements PredefinedQuery {

    private final String id;
    private final String queryString;
    private final PredefinedQueryConfigEntry queryConfig;
    private final ParsedQuery tupleOrBooleanParsedQuery;

    public AbstractPredefinedQuery(String id, String queryString, PredefinedQueryConfigEntry queryConfig,
                                   ParsedQuery tupleOrBooleanParsedQuery) {
        this.id = id;
        this.queryString = queryString;
        this.queryConfig = queryConfig;
        this.tupleOrBooleanParsedQuery = tupleOrBooleanParsedQuery;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ParsedQuery getTupleOrBooleanParsedQuery() {
        return tupleOrBooleanParsedQuery;
    }

    @Override
    public Optional<String> getName() {
        return queryConfig.getName();
    }

    @Override
    public Optional<String> getDescription() {
        return queryConfig.getDescription();
    }
}
