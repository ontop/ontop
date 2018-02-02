package it.unibz.inf.ontop.answering.resultset;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;

import java.util.NoSuchElementException;

public interface IterativeOBDAResultSet<E, X extends OntopQueryAnsweringException> extends OBDAResultSet {

    /* Behaves like java.util.Iterator.hasNext() and org.eclipse.rdf4j.common.iteration.Iteration.hasNext() */
    boolean hasNext() throws OntopConnectionException, OntopResultConversionException, X;

    /**
     * Behaves like java.util.Iterator.next() and org.eclipse.rdf4j.common.iteration.Iteration.next().
     * Does not behave like java.sql.ResultSet.next().
     */
    E next() throws OntopConnectionException, OntopResultConversionException, X, NoSuchElementException;
}
