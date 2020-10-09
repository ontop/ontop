package it.unibz.inf.ontop.rdf4j.predefined.impl;

import it.unibz.inf.ontop.answering.reformulation.input.*;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedGraphQuery;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public class PredefinedGraphQueryImpl extends AbstractPredefinedQuery<RDF4JConstructQuery> implements PredefinedGraphQuery {

    @Nullable
    private final Object expandContext;

    public PredefinedGraphQueryImpl(String id, RDF4JConstructQuery graphQuery, PredefinedQueryConfigEntry queryConfig) {
        super(id, graphQuery, queryConfig);
        this.expandContext = null;
    }

    public PredefinedGraphQueryImpl(String id, RDF4JConstructQuery graphQuery, PredefinedQueryConfigEntry queryConfig,
                                    Object expandContext) {
        super(id, graphQuery, queryConfig);
        this.expandContext = expandContext;
    }

    @Override
    public Optional<Map<String, Object>> getJsonLdFrame() {
        return queryConfig.getFrame();
    }

    @Override
    public Optional<Object> getExpandContext() {
        return Optional.ofNullable(expandContext);
    }

    @Override
    public ConstructTemplate getConstructTemplate() {
        return getInputQuery().getConstructTemplate();
    }
}
