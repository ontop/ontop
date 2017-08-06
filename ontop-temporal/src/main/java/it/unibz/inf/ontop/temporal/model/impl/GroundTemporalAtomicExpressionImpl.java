package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.temporal.model.GroundTemporalAtomicExpression;

import java.util.List;

public class GroundTemporalAtomicExpressionImpl  extends TemporalAtomicExpressionImpl implements GroundTemporalAtomicExpression {
    GroundTemporalAtomicExpressionImpl(Predicate predicate, List<Term> terms) {
        super(predicate, terms);
    }

    @Override
    public List<Constant> getTerms() {
        return (List<Constant>) super.getTerms();
    }
}
