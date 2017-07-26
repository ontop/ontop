package it.unibz.inf.ontop.owlrefplatform.owlapi;


import org.semanticweb.owlapi.model.OWLException;

public interface BooleanOWLResultSet extends OWLResultSet {

    boolean getValue() throws OWLException;
}
