package it.unibz.inf.ontop.answering.resultset;

import org.eclipse.rdf4j.model.Statement;

import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

public interface GraphResultSet<X extends OntopQueryAnsweringException> extends IterativeOBDAResultSet<RDFFact, X>, Iterable<Statement> {

    @Override
    RDFFact next() throws X;

}
