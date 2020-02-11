package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.Expression;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.Term;

import java.util.ArrayList;
import java.util.List;

public class ExpressionImpl extends FunctionalTermImpl implements Expression {

    protected ExpressionImpl(BooleanFunctionSymbol functor, List<Term> terms) {
        super(functor, terms);
    }

    @Override
    public BooleanFunctionSymbol getFunctionSymbol() {
        return (BooleanFunctionSymbol) super.getFunctionSymbol();
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
