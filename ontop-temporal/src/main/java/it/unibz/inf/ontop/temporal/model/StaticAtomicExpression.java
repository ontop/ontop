package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.Term;

import java.util.List;


public interface StaticAtomicExpression extends StaticExpression{

    public Predicate getPredicate();

    public List<? extends Term> getTerms();

}
