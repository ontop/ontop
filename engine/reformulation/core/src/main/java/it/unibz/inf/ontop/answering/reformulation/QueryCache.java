package it.unibz.inf.ontop.answering.reformulation;

import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;

/**
 * Cache of queries.
 *
 * Mutable class.
 *
 */
public interface QueryCache {
    ExecutableQuery get(InputQuery inputQuery);

    void put(InputQuery inputQuery, ExecutableQuery executableQuery);

    void clear();
}
