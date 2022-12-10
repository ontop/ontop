package it.unibz.inf.ontop.query.resultset.impl;

import it.unibz.inf.ontop.exception.OntopConnectionException;

@FunctionalInterface
public interface OntopConnectionCloseable {
    void close() throws OntopConnectionException;
}
