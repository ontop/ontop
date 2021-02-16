package it.unibz.inf.ontop.answering.resultset;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

public interface OntopCloseableIterator<X extends RDFFact, Y extends OntopConnectionException> extends AutoCloseable{

    boolean hasNext() throws OntopConnectionException, OntopResultConversionException;

    X next() throws Y;

    void close() throws Y;

}
