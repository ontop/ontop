package it.unibz.inf.ontop.owlrefplatform.owlapi.impl;

import com.google.common.collect.Iterators;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.OntopBindingSet;
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
import java.util.Iterator;
import java.util.List;

public class OntopOWLBindingSet implements OWLBindingSet {
	
    private final OntopBindingSet ontopBindingSet;

    public OntopOWLBindingSet(OntopBindingSet ontopBindingSet) {
        this.ontopBindingSet = ontopBindingSet;
    }

    @Override
    @Nonnull
    public Iterator<OWLBinding> iterator() {
        return Iterators.transform(ontopBindingSet.iterator(), OntopOWLBinding::new);
    }

    @Override
    public List<String> getBindingNames() throws OWLException {
        return ontopBindingSet.getBidingNames();
    }

    @Override
    public OWLBinding getBinding(String bindingName) throws OWLException {
        try {
            return new OntopOWLBinding(ontopBindingSet.getBinding(bindingName));
        } catch (OntopConnectionException | OntopResultConversionException e) {
            throw new OWLException(e);
        }

        // TODO(xiao): implement similar behavior as in RDF4J
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

    private OWLAPIIndividualTranslator translator = new OWLAPIIndividualTranslator();

    private OWLPropertyAssertionObject translate(Constant c) {
        if (c instanceof ObjectConstant)
            return translator.translate((ObjectConstant) c);
        else
            return translator.translate((ValueConstant) c);
    }
}
