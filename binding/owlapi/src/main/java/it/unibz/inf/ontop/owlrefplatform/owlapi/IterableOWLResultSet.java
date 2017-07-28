package it.unibz.inf.ontop.owlrefplatform.owlapi;

import org.semanticweb.owlapi.model.OWLException;

public interface IterableOWLResultSet<E> extends OWLResultSet {

    boolean hasNext() throws OWLException;

    E next() throws OWLException;
}
