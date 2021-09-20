package it.unibz.inf.ontop.answering.cache.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.cache.HTTPCacheHeaders;
import it.unibz.inf.ontop.injection.OntopSystemSettings;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HTTPCacheHeadersImpl implements HTTPCacheHeaders {

    private static final String CACHE_CONTROL_KEY =  "Cache-Control";
    private static final String VARY_KEY = "Vary";
    private static final String ACCEPT_KEY = "Accept";
    private final ImmutableMap<String, String> map;

    @Inject
    private HTTPCacheHeadersImpl(OntopSystemSettings settings) {
        map = settings.getHttpCacheControl()
                .map(v -> ImmutableMap.of(
                        CACHE_CONTROL_KEY, v,
                        VARY_KEY, ACCEPT_KEY))
                .orElseGet(ImmutableMap::of);
    }

    @Override
    public ImmutableMap<String, String> getMap() {
        return map;
    }
}
