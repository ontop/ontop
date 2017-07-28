package it.unibz.inf.ontop.owlrefplatform.owlapi.impl;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.answering.resultset.BooleanResultSet;
import it.unibz.inf.ontop.owlapi.OntopOWLException;
import it.unibz.inf.ontop.owlrefplatform.owlapi.BooleanOWLResultSet;
import org.semanticweb.owlapi.model.OWLException;

public class OntopBooleanOWLResultSet implements BooleanOWLResultSet {

    private final BooleanResultSet resultSet;

    public OntopBooleanOWLResultSet(BooleanResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public boolean getValue() throws OWLException {
        try {
            return resultSet.getValue();
        } catch (OntopConnectionException e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public void close() throws OWLException {
        try {
            resultSet.close();
        } catch (OntopConnectionException e) {
            throw new OntopOWLException(e);
        }
    }
}
