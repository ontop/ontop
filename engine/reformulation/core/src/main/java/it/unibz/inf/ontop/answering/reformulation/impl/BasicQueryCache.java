package it.unibz.inf.ontop.answering.reformulation.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic implementation. No memory management, no consideration for similar queries.
 *
 */
public class BasicQueryCache implements QueryCache {

    private final Map<InputQuery, ExecutableQuery> mutableMap;

    @Inject
    private BasicQueryCache() {
        mutableMap = new ConcurrentHashMap<>();
    }

    @Override
    public ExecutableQuery get(InputQuery inputQuery) {
        return mutableMap.get(inputQuery);
    }

    @Override
    public void put(InputQuery inputQuery, ExecutableQuery executableQuery) {
        mutableMap.put(inputQuery, executableQuery);
    }

    @Override
    public void clear() {
        mutableMap.clear();
    }
}
