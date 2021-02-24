package it.unibz.inf.ontop.answering.resultset;

import it.unibz.inf.ontop.exception.OntopConnectionException;

import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

public interface GraphResultSet extends IterativeOBDAResultSet<RDFFact,OntopQueryAnsweringException> {

    @Override
    RDFFact next() throws OntopQueryAnsweringException, OntopConnectionException;

    OntopCloseableIterator<RDFFact, OntopConnectionException> iterator();

}
