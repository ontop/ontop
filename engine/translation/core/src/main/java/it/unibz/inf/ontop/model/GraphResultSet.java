package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.ontology.Assertion;

public interface GraphResultSet<X extends OntopQueryAnsweringException> extends IterativeOBDAResultSet<X> {


    Assertion next() throws OntopConnectionException, OntopResultConversionException, X;

}
