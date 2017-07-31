package it.unibz.inf.ontop.owlapi.resultset;


import org.semanticweb.owlapi.model.OWLException;

public interface BooleanOWLResultSet extends OWLResultSet {

    boolean getValue() throws OWLException;
}
