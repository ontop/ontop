package it.unibz.inf.ontop.answering.reformulation.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.iq.IQ;

import javax.annotation.Nullable;

public class GuiceBasedQueryCache implements QueryCache {

    // NB: still present in more recent versions of Guava
    @SuppressWarnings("UnstableApiUsage")
    private final Cache<InputQuery, IQ> cache;

    @Inject
    private GuiceBasedQueryCache(OntopReformulationSettings settings) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(settings.getQueryCacheMaxSize())
                .build();
    }

    @Nullable
    @Override
    public IQ get(InputQuery inputQuery) {
        return cache.getIfPresent(inputQuery);
    }

    @Override
    public void put(InputQuery inputQuery, IQ executableQuery) {
        cache.put(inputQuery, executableQuery);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }
}
