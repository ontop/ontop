package it.unibz.inf.ontop.answering.resultset;

import it.unibz.inf.ontop.exception.OntopConnectionException;

import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

public interface GraphResultSet<X extends OntopQueryAnsweringException> extends IterativeOBDAResultSet<RDFFact, X> {

    @Override
    RDFFact next() throws X, OntopConnectionException;

    OntopCloseableIterator<RDFFact, OntopConnectionException> iterator();

}
