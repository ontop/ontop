package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.spec.ontology.Assertion;

public interface GraphResultSet<X extends OntopQueryAnsweringException> extends IterativeOBDAResultSet<Assertion, X> {

    @Override
    Assertion next() throws X;
}
