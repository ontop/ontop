package it.unibz.inf.ontop.owlapi.resultset;


import org.semanticweb.owlapi.model.OWLException;

public interface OWLResultSet extends AutoCloseable {

    @Override
    void close() throws OWLException;
}
