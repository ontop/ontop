package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

import java.util.List;

public interface AtomicExpression extends DatalogMTLExpression {
    Predicate getPredicate();

    List<? extends Term> getTerms();
}
