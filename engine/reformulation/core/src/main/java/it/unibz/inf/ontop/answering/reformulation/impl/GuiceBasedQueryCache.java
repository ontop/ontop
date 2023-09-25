package it.unibz.inf.ontop.answering.reformulation.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.iq.IQ;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Takes into account the full query context.
 * A future implementation could select only certain aspects of the query context.
 */
public class GuiceBasedQueryCache implements QueryCache {

    private final Cache<Map.Entry<KGQuery<?>, QueryContext>, IQ> cache;

    @Inject
    private GuiceBasedQueryCache(OntopReformulationSettings settings) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(settings.getQueryCacheMaxSize())
                .build();
    }

    @Nullable
    @Override
    public IQ get(KGQuery<?> inputQuery, QueryContext queryContext) {
        return cache.getIfPresent(Maps.immutableEntry(inputQuery, queryContext));
    }

    @Override
    public void put(KGQuery<?> inputQuery, QueryContext queryContext, IQ executableQuery) {
        cache.put(Maps.immutableEntry(inputQuery, queryContext), executableQuery);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }
}
