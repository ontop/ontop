package it.unibz.inf.ontop.answering.reformulation;

import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.iq.IQ;

/**
 * Cache of queries.
 *
 * Mutable class.
 *
 */
public interface QueryCache {
    IQ get(KGQuery<?> inputQuery, QueryContext queryContext);

    void put(KGQuery<?> inputQuery, QueryContext queryContext, IQ executableQuery);

    void clear();
}
