package it.unibz.inf.ontop.query.impl;

import it.unibz.inf.ontop.query.ConstructTemplate;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;

class ConstructQuerySplit {
    private final ConstructTemplate constructTemplate;
    private final ParsedTupleQuery selectParsedQuery;

    ConstructQuerySplit(ConstructTemplate constructTemplate, ParsedTupleQuery selectQuery) {
        this.constructTemplate = constructTemplate;
        this.selectParsedQuery = selectQuery;
    }

    public ConstructTemplate getConstructTemplate() {
        return constructTemplate;
    }

    public ParsedTupleQuery getSelectParsedQuery() {
        return selectParsedQuery;
    }
}
