package it.unibz.inf.ontop.answering.reformulation.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.iq.IQ;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Takes into account the full query context and the forNativeConsumption flag.
 * A future implementation could select only certain aspects of the query context.
 */
public class GuiceBasedQueryCache implements QueryCache {

    private static final class CacheKey {
        private final KGQuery<?> inputQuery;
        private final QueryContext queryContext;
        private final boolean forNativeConsumption;

        CacheKey(KGQuery<?> inputQuery, QueryContext queryContext, boolean forNativeConsumption) {
            this.inputQuery = inputQuery;
            this.queryContext = queryContext;
            this.forNativeConsumption = forNativeConsumption;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;
            CacheKey other = (CacheKey) o;
            return forNativeConsumption == other.forNativeConsumption
                    && Objects.equals(inputQuery, other.inputQuery)
                    && Objects.equals(queryContext, other.queryContext);
        }

        @Override
        public int hashCode() {
            return Objects.hash(inputQuery, queryContext, forNativeConsumption);
        }
    }

    private final Cache<CacheKey, IQ> cache;

    @Inject
    private GuiceBasedQueryCache(OntopReformulationSettings settings) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(settings.getQueryCacheMaxSize())
                .build();
    }

    @Nullable
    @Override
    public IQ get(KGQuery<?> inputQuery, QueryContext queryContext, boolean forNativeConsumption) {
        return cache.getIfPresent(new CacheKey(inputQuery, queryContext, forNativeConsumption));
    }

    @Override
    public void put(KGQuery<?> inputQuery, QueryContext queryContext, boolean forNativeConsumption, IQ executableQuery) {
        cache.put(new CacheKey(inputQuery, queryContext, forNativeConsumption), executableQuery);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }
}
