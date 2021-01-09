package it.unibz.inf.ontop.owlapi.resultset;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

import java.util.Iterator;
import java.util.Set;

public interface OWLBindingSet extends Iterable<OWLBinding> {

    @Override
    Iterator<OWLBinding> iterator();

    Set<String> getBindingNames() throws OWLException;

    OWLBinding getBinding(String bindingName) throws OWLException;

    OWLPropertyAssertionObject getOWLPropertyAssertionObject(String bindingName) throws OWLException;

    OWLIndividual getOWLIndividual(String bindingName) throws OWLException;

    OWLNamedIndividual getOWLNamedIndividual(String bindingName) throws OWLException;

    OWLAnonymousIndividual getOWLAnonymousIndividual(String bindingName) throws OWLException;

    OWLLiteral getOWLLiteral(String bindingName) throws OWLException;

    OWLObject getOWLObject(String bindingName) throws OWLException;
}
