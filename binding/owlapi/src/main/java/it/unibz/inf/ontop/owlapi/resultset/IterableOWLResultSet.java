package it.unibz.inf.ontop.owlapi.resultset;

import org.semanticweb.owlapi.model.OWLException;

public interface IterableOWLResultSet<E> extends OWLResultSet {

    boolean hasNext() throws OWLException;

    E next() throws OWLException;
}
