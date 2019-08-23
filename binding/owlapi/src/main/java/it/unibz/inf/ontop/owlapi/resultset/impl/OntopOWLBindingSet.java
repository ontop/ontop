package it.unibz.inf.ontop.owlapi.resultset.impl;

import com.google.common.collect.Iterators;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.owlapi.resultset.OWLBinding;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

public class OntopOWLBindingSet implements OWLBindingSet {

    private final OntopBindingSet ontopBindingSet;
    private final OWLAPIIndividualTranslator translator;

    public OntopOWLBindingSet(OntopBindingSet ontopBindingSet) {
        this.ontopBindingSet = ontopBindingSet;
        this.translator = new OWLAPIIndividualTranslator();
    }

    @Override
    @Nonnull
    public Iterator<OWLBinding> iterator() {
        return Iterators.transform(ontopBindingSet.iterator(), OntopOWLBinding::new);
    }

    @Override
    public List<String> getBindingNames() throws OWLException {
        return ontopBindingSet.getBindingNames();
    }

    @Override
    public OWLBinding getBinding(String bindingName) throws OWLException {
        final OntopBinding ontopBinding = ontopBindingSet.getBinding(bindingName);
        if (ontopBinding == null) {
            return null;
        } else {
            return new OntopOWLBinding(ontopBinding);
        }
    }


    @Override
    public OWLPropertyAssertionObject getOWLPropertyAssertionObject(int column) throws OWLException {
        try {
            return translate(ontopBindingSet.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e + " Column: " + column);
        }
    }

    @Override
    public OWLIndividual getOWLIndividual(int column) throws OWLException {
        try {
            return (OWLIndividual) translate(ontopBindingSet.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLIndividual getOWLIndividual(String column) throws OWLException {
        try {
            return (OWLIndividual) translate(ontopBindingSet.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLNamedIndividual getOWLNamedIndividual(int column) throws OWLException {
        try {
            return (OWLNamedIndividual) translate(ontopBindingSet.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLAnonymousIndividual getOWLAnonymousIndividual(int column) throws OWLException {
        try {
            return (OWLAnonymousIndividual) translate(ontopBindingSet.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }


    @Override
    public OWLLiteral getOWLLiteral(int column) throws OWLException {
        try {
            return (OWLLiteral) translate(ontopBindingSet.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLLiteral getOWLLiteral(String column) throws OWLException {
        try {
            return (OWLLiteral) translate(ontopBindingSet.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLObject getOWLObject(int column) throws OWLException {
        try {
            return translate(ontopBindingSet.getConstant(column));
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

    private OWLPropertyAssertionObject translate(Constant c) {
        if (c instanceof ObjectConstant)
            return translator.translate((ObjectConstant) c);
        else
            return translator.translate((ValueConstant) c);
    }

    @Override
    public String toString() {
        return ontopBindingSet.toString();
    }
}
