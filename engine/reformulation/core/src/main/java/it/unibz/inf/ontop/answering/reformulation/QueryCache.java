package it.unibz.inf.ontop.answering.reformulation;

import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.iq.IQ;

/**
 * Cache of queries.
 *
 * Mutable class.
 *
 */
public interface QueryCache {
    IQ get(KGQuery inputQuery);

    void put(KGQuery inputQuery, IQ executableQuery);

    void clear();
}
