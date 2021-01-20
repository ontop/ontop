package it.unibz.inf.ontop.owlapi.resultset.impl;

import com.google.common.collect.Iterators;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.RDFLiteralConstant;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.owlapi.resultset.OWLBinding;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class OntopOWLBindingSet implements OWLBindingSet {

    private final OntopBindingSet ontopBindingSet;
    private final OWLAPIIndividualTranslator translator;
    private final byte[] salt;

    public OntopOWLBindingSet(OntopBindingSet ontopBindingSet, byte[] salt) {
        this.ontopBindingSet = ontopBindingSet;
        this.salt = salt;
        this.translator = new OWLAPIIndividualTranslator();
    }

    @Override
    @Nonnull
    public Iterator<OWLBinding> iterator() {
        return Iterators.transform(ontopBindingSet.iterator(),
                ontopBinding -> new OntopOWLBinding(ontopBinding, translator, salt));
    }

    @Override
    public Set<String> getBindingNames() {
        return Arrays.stream(ontopBindingSet.getBindingNames()).collect(Collectors.toSet());
    }

    @Override
    public OWLBinding getBinding(String bindingName) {
        OntopBinding ontopBinding = ontopBindingSet.getBinding(bindingName);
        if (ontopBinding != null) {
            return new OntopOWLBinding(ontopBinding, translator, salt);
        }
        return null;
    }

    @Override
    public OWLPropertyAssertionObject getOWLPropertyAssertionObject(String bindingName) throws OWLException {
        try {
            return translate(ontopBindingSet.getConstant(bindingName));
        } catch (Exception e) {
            throw new OntopOWLException(e + " Column: " + bindingName);
        }
    }

    @Override
    public OWLIndividual getOWLIndividual(String bindingName) throws OWLException {
        try {
            return (OWLIndividual) translate(ontopBindingSet.getConstant(bindingName));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLNamedIndividual getOWLNamedIndividual(String bindingName) throws OWLException {
        try {
            return (OWLNamedIndividual) translate(ontopBindingSet.getConstant(bindingName));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLAnonymousIndividual getOWLAnonymousIndividual(String bindingName ) throws OWLException {
        try {
            return (OWLAnonymousIndividual) translate(ontopBindingSet.getConstant(bindingName));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLLiteral getOWLLiteral(String bindingName) throws OWLException {
        try {
            return (OWLLiteral) translate(ontopBindingSet.getConstant(bindingName));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLObject getOWLObject(String column) throws OWLException {
        try {
            return translate(ontopBindingSet.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public String toString() {
        return ontopBindingSet.toString();
    }

    private OWLPropertyAssertionObject translate(Constant c) {
        if (c instanceof ObjectConstant)
            return translator.translate((ObjectConstant) c, salt);
        else
            return translator.translate((RDFLiteralConstant) c);
    }
}
