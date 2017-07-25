package it.unibz.inf.ontop.owlrefplatform.owlapi;


import org.semanticweb.owlapi.model.OWLException;

public interface OntopOWLResultSet extends AutoCloseable {

    boolean hasNext() throws OWLException;

    @Override
    void close() throws OWLException;
}
