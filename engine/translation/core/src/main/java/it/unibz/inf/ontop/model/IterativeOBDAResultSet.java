package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;

public interface IterativeOBDAResultSet<X extends OntopQueryAnsweringException> extends OBDAResultSet {

    boolean hasNext() throws OntopConnectionException, OntopResultConversionException, X;
}
