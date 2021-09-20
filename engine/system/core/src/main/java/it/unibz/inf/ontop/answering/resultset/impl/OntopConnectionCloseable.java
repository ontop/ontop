package it.unibz.inf.ontop.answering.resultset.impl;

import it.unibz.inf.ontop.exception.OntopConnectionException;

@FunctionalInterface
public interface OntopConnectionCloseable {
    void close() throws OntopConnectionException;
}
