package it.unibz.inf.ontop.owlrefplatform.core.execution;

import com.google.inject.Inject;
import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.core.QueryCache;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic implementation. No timeout.
 *
 */
public class BasicQueryCache implements QueryCache {

    private final Map<ParsedQuery, ExecutableQuery> mutableMap;

    @Inject
    private BasicQueryCache() {
        mutableMap = new ConcurrentHashMap<>();
    }

    @Override
    public ExecutableQuery get(ParsedQuery sparqlTree) {
        return mutableMap.get(sparqlTree);
    }

    @Override
    public void put(ParsedQuery sparqlTree, ExecutableQuery executableQuery) {
        mutableMap.put(sparqlTree, executableQuery);
    }

    @Override
    public void clear() {
        mutableMap.clear();
    }
}
