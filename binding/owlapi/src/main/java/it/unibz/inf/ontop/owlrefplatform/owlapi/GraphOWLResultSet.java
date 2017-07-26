package it.unibz.inf.ontop.owlrefplatform.owlapi;


import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;

public interface GraphOWLResultSet extends IterableOWLResultSet {

    OWLAxiom next() throws OWLException;
}
