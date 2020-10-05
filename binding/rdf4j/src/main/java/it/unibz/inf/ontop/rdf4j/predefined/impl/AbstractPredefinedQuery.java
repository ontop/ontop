package it.unibz.inf.ontop.rdf4j.predefined.impl;

import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedQuery;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Optional;

public class AbstractPredefinedQuery<Q extends InputQuery> implements PredefinedQuery<Q> {

    private final String id;
    private final PredefinedQueryConfigEntry queryConfig;
    private final Q inputQuery;

    public AbstractPredefinedQuery(String id, Q inputQuery, PredefinedQueryConfigEntry queryConfig) {
        this.id = id;
        this.queryConfig = queryConfig;
        this.inputQuery = inputQuery;
    }

    @Override
    public Q getInputQuery() {
        return inputQuery;
    }

    @Override
    public String getId() {
        return id;
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
