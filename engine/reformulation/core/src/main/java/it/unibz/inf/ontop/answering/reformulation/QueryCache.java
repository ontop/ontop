package it.unibz.inf.ontop.answering.reformulation;

import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.iq.IQ;

/**
 * Cache of queries.
 *
 * Mutable class.
 *
 */
public interface QueryCache {
    IQ get(InputQuery inputQuery);

    void put(InputQuery inputQuery, IQ executableQuery);

    void clear();
}
