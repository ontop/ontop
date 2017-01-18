package it.unibz.inf.ontop.owlrefplatform.core;

import org.eclipse.rdf4j.query.parser.ParsedQuery;

/**
 * Cache of queries.
 *
 * Mutable class.
 */
public interface QueryCache {
    ExecutableQuery get(ParsedQuery sparqlTree);

    void put(ParsedQuery sparqlTree, ExecutableQuery executableQuery);

    void clear();
}
