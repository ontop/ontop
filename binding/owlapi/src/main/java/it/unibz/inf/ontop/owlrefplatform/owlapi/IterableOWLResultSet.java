package it.unibz.inf.ontop.owlrefplatform.owlapi;

import org.semanticweb.owlapi.model.OWLException;

public interface IterableOWLResultSet extends OWLResultSet {

    boolean hasNext() throws OWLException;
}
