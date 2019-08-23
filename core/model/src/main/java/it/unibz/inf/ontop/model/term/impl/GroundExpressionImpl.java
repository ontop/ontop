package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.GroundFunctionalTerm;
import it.unibz.inf.ontop.model.term.GroundTerm;

public class GroundExpressionImpl extends ImmutableExpressionImpl implements GroundFunctionalTerm {

    protected GroundExpressionImpl(OperationPredicate functor, GroundTerm... terms) {
        super(functor, terms);
    }

    protected GroundExpressionImpl(OperationPredicate functor, ImmutableList<? extends GroundTerm> terms) {
        super(functor, terms);
    }

    @Override
    public ImmutableList<? extends GroundTerm> getTerms() {
        return (ImmutableList<? extends GroundTerm>)super.getTerms();
    }

    @Override
    public boolean isGround() {
        return true;
    }

    @Override
    public boolean isVar2VarEquality() {
        return false;
    }
}
