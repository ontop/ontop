package it.unibz.inf.ontop.owlrefplatform.owlapi.impl;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.model.TupleResultSet;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.owlapi.OWLAPIIndividualTranslator;
import it.unibz.inf.ontop.owlapi.OntopOWLException;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OWLBinding;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OWLBindingSet;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OntopOWLBindingSet implements OWLBindingSet {
    private final TupleResultSet res;

    public OntopOWLBindingSet(TupleResultSet res) {
        this.res = res;
    }

    @Override
    @Nonnull
    public Iterator<OWLBinding> iterator() {

        List<OWLBinding> bindings = new ArrayList<>();
        final List<String> bindingNames;
        try {
            bindingNames = getBindingNames();
            for (String name : bindingNames) {
                bindings.add(new OntopOWLBinding(name, getOWLObject(name)));
            }
        } catch (OWLException e) {
            e.printStackTrace();
        }
        return bindings.iterator();
    }

    @Override
    public List<String> getBindingNames() throws OWLException {
        try {
            return res.getSignature();
        } catch (OntopConnectionException e) {
            throw new OWLException(e);
        }
    }

    @Override
    public OWLBinding getBinding(String bindingName) throws OWLException {
        OWLObject value = getOWLObject(bindingName);
        if (value != null) {
            return new OntopOWLBinding(bindingName, value);
        }
        return null;
    }


    @Override
    public OWLPropertyAssertionObject getOWLPropertyAssertionObject(int column) throws OWLException {
        try {
            return translate(res.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e + " Column: " + column);
        }
    }

    @Override
    public OWLIndividual getOWLIndividual(int column) throws OWLException {
        try {
            return (OWLIndividual) translate(res.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLIndividual getOWLIndividual(String column) throws OWLException {
        try {
            return (OWLIndividual) translate(res.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLNamedIndividual getOWLNamedIndividual(int column) throws OWLException {
        try {
            return (OWLNamedIndividual) translate(res.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLAnonymousIndividual getOWLAnonymousIndividual(int column) throws OWLException {
        try {
            return (OWLAnonymousIndividual) translate(res.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }


    @Override
    public OWLLiteral getOWLLiteral(int column) throws OWLException {
        try {
            return (OWLLiteral) translate(res.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLLiteral getOWLLiteral(String column) throws OWLException {
        try {
            return (OWLLiteral) translate(res.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLObject getOWLObject(int column) throws OWLException {
        try {
            return translate(res.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLObject getOWLObject(String column) throws OWLException {
        try {
            return translate(res.getConstant(column));
        } catch (Exception e) {
            throw new OntopOWLException(e);
        }
    }

    private OWLAPIIndividualTranslator translator = new OWLAPIIndividualTranslator();

    private OWLPropertyAssertionObject translate(Constant c) {
        if (c instanceof ObjectConstant)
            return translator.translate((ObjectConstant) c);
        else
            return translator.translate((ValueConstant) c);
    }
}
