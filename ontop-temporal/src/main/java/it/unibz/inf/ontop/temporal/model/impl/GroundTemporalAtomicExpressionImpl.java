package it.unibz.inf.ontop.temporal.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.temporal.model.GroundTemporalAtomicExpression;
import java.util.List;

public class GroundTemporalAtomicExpressionImpl  extends TemporalAtomicExpressionImpl implements GroundTemporalAtomicExpression {
    GroundTemporalAtomicExpressionImpl(AtomPredicate predicate, List<Term> terms) {
        super(predicate, terms);
    }

    @Override
    public ImmutableList<Constant> getTerms() {
        return (ImmutableList<Constant>) super.getTerms();
    }
}
