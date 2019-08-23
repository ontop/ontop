package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.Expression;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.Term;

import java.util.ArrayList;
import java.util.List;

public class ExpressionImpl extends FunctionalTermImpl implements Expression {

    protected ExpressionImpl(OperationPredicate functor, Term... terms) {
        super(functor, terms);
    }

    protected ExpressionImpl(OperationPredicate functor, List<Term> terms) {
        super(functor, terms);
    }

    @Override
    public OperationPredicate getFunctionSymbol() {
        return (OperationPredicate) super.getFunctionSymbol();
    }

    @Override
    public Expression clone() {
        List<Term> newTerms = new ArrayList<>();
        for (Term term: getTerms()) {
            newTerms.add(term.clone());
        }
        return new ExpressionImpl(getFunctionSymbol(), newTerms);
    }

}
