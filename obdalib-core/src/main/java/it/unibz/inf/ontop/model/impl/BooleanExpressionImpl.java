package it.unibz.inf.ontop.model.impl;

import it.unibz.inf.ontop.model.BooleanExpression;
import it.unibz.inf.ontop.model.OperationPredicate;
import it.unibz.inf.ontop.model.Term;

import java.util.ArrayList;
import java.util.List;

public class BooleanExpressionImpl extends FunctionalTermImpl implements BooleanExpression {

    protected BooleanExpressionImpl(OperationPredicate functor, Term... terms) {
        super(functor, terms);
    }

    protected BooleanExpressionImpl(OperationPredicate functor, List<Term> terms) {
        super(functor, terms);
    }

    @Override
    public OperationPredicate getFunctionSymbol() {
        return (OperationPredicate) super.getFunctionSymbol();
    }

    @Override
    public BooleanExpression clone() {
        List<Term> newTerms = new ArrayList<>();
        for (Term term: getTerms()) {
            newTerms.add(term.clone());
        }
        return new BooleanExpressionImpl(getFunctionSymbol(), newTerms);
    }

}
