package it.unibz.inf.ontop.owlrefplatform.owlapi;

import org.semanticweb.owlapi.model.*;

import java.util.List;

public interface TupleOWLResultSet extends IterableOWLResultSet {

    int getColumnCount() throws OWLException;

    List<String> getSignature() throws OWLException;

    int getFetchSize() throws OWLException;

    OWLPropertyAssertionObject getOWLPropertyAssertionObject(int column) throws OWLException;

    OWLIndividual getOWLIndividual(int column) throws OWLException;

    OWLIndividual getOWLIndividual(String column) throws OWLException;

    OWLLiteral getOWLLiteral(int column) throws OWLException;

    OWLLiteral getOWLLiteral(String column) throws OWLException;

    OWLObject getOWLObject(int column) throws OWLException;

    OWLObject getOWLObject(String column) throws OWLException;
}
