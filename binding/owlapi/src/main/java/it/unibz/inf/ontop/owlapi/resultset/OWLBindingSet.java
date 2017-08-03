package it.unibz.inf.ontop.owlapi.resultset;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

import java.util.Iterator;
import java.util.List;

public interface OWLBindingSet extends Iterable<OWLBinding> {

    @Override
    Iterator<OWLBinding> iterator();

    List<String> getBindingNames() throws OWLException;

    OWLBinding getBinding(String bindingName) throws OWLException;

    OWLPropertyAssertionObject getOWLPropertyAssertionObject(int column) throws OWLException;

    OWLIndividual getOWLIndividual(int column) throws OWLException;

    OWLIndividual getOWLIndividual(String column) throws OWLException;

    OWLNamedIndividual getOWLNamedIndividual(int column) throws OWLException;

    OWLAnonymousIndividual getOWLAnonymousIndividual(int column) throws OWLException;

    OWLLiteral getOWLLiteral(int column) throws OWLException;

    OWLLiteral getOWLLiteral(String column) throws OWLException;

    OWLObject getOWLObject(int column) throws OWLException;

    OWLObject getOWLObject(String column) throws OWLException;
}
