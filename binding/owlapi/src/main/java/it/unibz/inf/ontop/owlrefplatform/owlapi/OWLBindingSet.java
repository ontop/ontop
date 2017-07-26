package it.unibz.inf.ontop.owlrefplatform.owlapi;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

public interface OWLBindingSet {
    OWLPropertyAssertionObject getOWLPropertyAssertionObject(int column) throws OWLException;

    OWLIndividual getOWLIndividual(int column) throws OWLException;

    OWLIndividual getOWLIndividual(String column) throws OWLException;

    OWLLiteral getOWLLiteral(int column) throws OWLException;

    OWLLiteral getOWLLiteral(String column) throws OWLException;

    OWLObject getOWLObject(int column) throws OWLException;

    OWLObject getOWLObject(String column) throws OWLException;
}
