package unibz.inf.ontop.model.impl;

import unibz.inf.ontop.model.BooleanExpression;
import unibz.inf.ontop.model.BooleanOperationPredicate;
import unibz.inf.ontop.model.Term;

import java.util.ArrayList;
import java.util.List;

public class BooleanExpressionImpl extends FunctionalTermImpl implements BooleanExpression {

    protected BooleanExpressionImpl(BooleanOperationPredicate functor, Term... terms) {
        super(functor, terms);
    }

    protected BooleanExpressionImpl(BooleanOperationPredicate functor, List<Term> terms) {
        super(functor, terms);
    }

    @Override
    public BooleanExpression clone() {
        List<Term> newTerms = new ArrayList<>();
        for (Term term: getTerms()) {
            newTerms.add(term.clone());
        }
        return new BooleanExpressionImpl((BooleanOperationPredicate)getFunctionSymbol(), newTerms);
    }

}
