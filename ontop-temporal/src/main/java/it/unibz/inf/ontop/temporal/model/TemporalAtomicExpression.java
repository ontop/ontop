package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.Term;

import java.util.List;

public interface TemporalAtomicExpression extends TemporalExpression{

    public Predicate getPredicate();

    public List<Term> getTerms();
}
