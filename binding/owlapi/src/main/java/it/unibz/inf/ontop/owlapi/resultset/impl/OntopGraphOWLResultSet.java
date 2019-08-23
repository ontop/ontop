package it.unibz.inf.ontop.owlapi.resultset.impl;

import it.unibz.inf.ontop.answering.resultset.GraphResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.owlapi.resultset.GraphOWLResultSet;
import it.unibz.inf.ontop.spec.ontology.*;
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
            return convertAssertion(graphResultSet.next());
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

    OWLAxiom convertAssertion(Assertion assertion) {
        if (assertion instanceof ClassAssertion) {
            return translator.translate((ClassAssertion) assertion);
        }
        else if (assertion instanceof ObjectPropertyAssertion) {
            return translator.translate((ObjectPropertyAssertion) assertion);
        }
        else if (assertion instanceof DataPropertyAssertion) {
            return translator.translate((DataPropertyAssertion) assertion);
        }
        else if (assertion instanceof AnnotationAssertion) {
            return translator.translate((AnnotationAssertion) assertion);
        }
        else
            throw new UnsupportedAssertionException(assertion);
    }


    private static class UnsupportedAssertionException extends OntopInternalBugException {
        UnsupportedAssertionException(Assertion assertion) {
            super("Unsupported assertion (cannot be converted to OWLAPI): " + assertion);
        }
    }
}
