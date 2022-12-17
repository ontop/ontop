package it.unibz.inf.ontop.query.resultset;


import it.unibz.inf.ontop.exception.OntopConnectionException;

/**
 * A common interface for TupleResultSet, BooleanResultSet and GraphResultSet
 */
public interface OBDAResultSet extends AutoCloseable {

    @Override
    void close() throws OntopConnectionException;
}
