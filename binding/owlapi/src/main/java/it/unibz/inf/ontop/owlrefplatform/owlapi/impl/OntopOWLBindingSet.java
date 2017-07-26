package it.unibz.inf.ontop.owlrefplatform.owlapi.impl;

import it.unibz.inf.ontop.model.TupleResultSet;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.owlapi.OWLAPIIndividualTranslator;
import it.unibz.inf.ontop.owlapi.OntopOWLException;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OWLBindingSet;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

public class OntopOWLBindingSet implements OWLBindingSet {
    private final TupleResultSet res;

    public OntopOWLBindingSet(TupleResultSet res) {
        this.res = res;
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
            return translator.translate((ObjectConstant)c);
        else
            return translator.translate((ValueConstant)c);
    }
}
