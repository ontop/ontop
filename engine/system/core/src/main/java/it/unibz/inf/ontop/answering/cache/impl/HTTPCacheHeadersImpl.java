package it.unibz.inf.ontop.answering.cache.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.cache.HTTPCacheHeaders;
import it.unibz.inf.ontop.injection.OntopSystemSettings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class HTTPCacheHeadersImpl implements HTTPCacheHeaders {

    private static final String CACHE_CONTROL_KEY =  "Cache-Control";
    private final ImmutableMap<String, String> map;

    @Inject
    private HTTPCacheHeadersImpl(OntopSystemSettings settings) {
        String cacheControlValue = Stream.of(settings.getHttpMaxAge()
                        .map(i -> "max-age=" + i),
                settings.getHttpStaleWhileRevalidate()
                        .map(i -> "stale-while-revalidate=" + i),
                settings.getHttpStaleIfError()
                        .map(i -> "stale-if-error=" + i))
                .flatMap(e -> e.map(Stream::of)
                        .orElseGet(Stream::empty))
                .collect(Collectors.joining(", "));

        map = cacheControlValue.isEmpty()
                ? ImmutableMap.of()
                : ImmutableMap.of(CACHE_CONTROL_KEY, cacheControlValue);
    }

    @Override
    public ImmutableMap<String, String> getMap() {
        return map;
    }
}
