package it.unibz.inf.ontop.owlrefplatform.owlapi;


import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;

public interface OntopOWLGraphResultSet extends OntopOWLResultSet {

    OWLAxiom next() throws OWLException;
}
