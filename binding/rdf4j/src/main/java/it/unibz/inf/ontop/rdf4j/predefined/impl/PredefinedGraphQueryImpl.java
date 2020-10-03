package it.unibz.inf.ontop.rdf4j.predefined.impl;

import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedGraphQuery;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Optional;

public class PredefinedGraphQueryImpl extends AbstractPredefinedQuery implements PredefinedGraphQuery {

    private final ConstructTemplate constructTemplate;

    public PredefinedGraphQueryImpl(String id, String queryString, ConstructTemplate constructTemplate,
                                    ParsedQuery tupleParsedQuery, PredefinedQueryConfigEntry queryConfig) {
        super(id, queryString, queryConfig, tupleParsedQuery);
        this.constructTemplate = constructTemplate;
    }

    @Override
    public ConstructTemplate getConstructTemplate() {
        return constructTemplate;
    }

    /**
     * TODO: get it
     */
    @Override
    public Optional<String> getJsonLdFrame() {
        return Optional.empty();
    }
}
