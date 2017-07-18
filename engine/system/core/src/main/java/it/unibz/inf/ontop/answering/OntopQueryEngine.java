package it.unibz.inf.ontop.answering;


import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.owlrefplatform.core.OntopConnection;

public interface OntopQueryEngine extends AutoCloseable {

    void close() throws OntopConnectionException;

    /**
     * Gets a OntopConnection usually coming from a connection pool.
     */
    OntopConnection getConnection() throws OntopConnectionException;
}
