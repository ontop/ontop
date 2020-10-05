package it.unibz.inf.ontop.rdf4j.predefined.impl;

import it.unibz.inf.ontop.answering.reformulation.input.ConstructQuery;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.reformulation.input.GraphSPARQLQuery;
import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedGraphQuery;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Optional;

public class PredefinedGraphQueryImpl extends AbstractPredefinedQuery<ConstructQuery> implements PredefinedGraphQuery {

    public PredefinedGraphQueryImpl(String id, ConstructQuery graphQuery, PredefinedQueryConfigEntry queryConfig) {
        super(id, graphQuery, queryConfig);
    }

    /**
     * TODO: get it
     */
    @Override
    public Optional<String> getJsonLdFrame() {
        return Optional.empty();
    }

    @Override
    public ConstructTemplate getConstructTemplate() {
        return getInputQuery().getConstructTemplate();
    }
}
