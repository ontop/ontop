package it.unibz.inf.ontop.owlrefplatform.core.execution;

import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.core.QueryCache;
import org.openrdf.query.parser.ParsedQuery;

/**
 * Does not cache anything.
 */
public class DummyQueryCache implements QueryCache {

    @Override
    public ExecutableQuery get(ParsedQuery sparqlTree) {
        return null;
    }

    @Override
    public void put(ParsedQuery sparqlTree, ExecutableQuery executableQuery) {
    }

    @Override
    public void clear() {
    }
}
