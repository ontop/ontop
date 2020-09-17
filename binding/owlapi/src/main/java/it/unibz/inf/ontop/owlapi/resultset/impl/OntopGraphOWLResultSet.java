package it.unibz.inf.ontop.owlapi.resultset.impl;

import it.unibz.inf.ontop.answering.resultset.GraphResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.owlapi.resultset.GraphOWLResultSet;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;


public class OntopGraphOWLResultSet implements GraphOWLResultSet {

    private final GraphResultSet graphResultSet;
    private final OWLAPIIndividualTranslator translator;

    public OntopGraphOWLResultSet(GraphResultSet graphResultSet) {
        this.graphResultSet = graphResultSet;
        this.translator = new OWLAPIIndividualTranslator();
    }

    @Override
    public boolean hasNext() throws OWLException {
        try {
            return graphResultSet.hasNext();
        } catch (OntopConnectionException | OntopQueryAnsweringException e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public OWLAxiom next() throws OWLException {
        try {
            return translator.translate(graphResultSet.next());
        } catch (OntopQueryAnsweringException e) {
            throw new OntopOWLException(e);
        }
    }

    @Override
    public void close() throws OWLException {
        try {
            graphResultSet.close();
        } catch (OntopConnectionException e) {
            throw new OntopOWLException(e);
        }
    }
}
