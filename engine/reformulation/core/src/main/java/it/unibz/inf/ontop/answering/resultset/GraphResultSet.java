package it.unibz.inf.ontop.answering.resultset;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.QueryEvaluationException;

import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

public interface GraphResultSet<X extends OntopQueryAnsweringException, Y extends QueryEvaluationException> extends IterativeOBDAResultSet<RDFFact, X> {

    @Override
    RDFFact next() throws X;

    CloseableIteration<Statement, Y> iterator();

}
