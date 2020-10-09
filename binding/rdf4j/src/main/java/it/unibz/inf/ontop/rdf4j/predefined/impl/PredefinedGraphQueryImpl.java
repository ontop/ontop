package it.unibz.inf.ontop.rdf4j.predefined.impl;

import it.unibz.inf.ontop.answering.reformulation.input.*;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedGraphQuery;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry;

import java.util.Map;
import java.util.Optional;

public class PredefinedGraphQueryImpl extends AbstractPredefinedQuery<RDF4JConstructQuery> implements PredefinedGraphQuery {

    public PredefinedGraphQueryImpl(String id, RDF4JConstructQuery graphQuery, PredefinedQueryConfigEntry queryConfig) {
        super(id, graphQuery, queryConfig);
    }

    @Override
    public Optional<Map<String, Object>> getJsonLdFrame() {
        return queryConfig.getFrame();
    }

    @Override
    public ConstructTemplate getConstructTemplate() {
        return getInputQuery().getConstructTemplate();
    }
}
