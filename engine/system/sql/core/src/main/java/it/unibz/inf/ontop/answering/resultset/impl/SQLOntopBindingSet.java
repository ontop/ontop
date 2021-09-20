package it.unibz.inf.ontop.answering.resultset.impl;

import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;

public class SQLOntopBindingSet extends AbstractOntopBindingSet implements OntopBindingSet {

    public SQLOntopBindingSet(OntopBinding[] bindings) {
        super(bindings);
    }

    public static class InvalidTermAsResultException extends OntopInternalBugException {
        InvalidTermAsResultException(ImmutableTerm term) {
            super("Term " + term + " does not evaluate to a constant");
        }
    }

    public static class InvalidConstantTypeInResultException extends OntopInternalBugException {
        InvalidConstantTypeInResultException (String message) {
            super(message);
        }
    }
}