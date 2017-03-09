package it.unibz.inf.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Expression;
import it.unibz.inf.ontop.model.GroundFunctionalTerm;
import it.unibz.inf.ontop.model.GroundTerm;
import it.unibz.inf.ontop.model.OperationPredicate;

public class GroundExpressionImpl extends ImmutableExpressionImpl implements GroundFunctionalTerm {

    protected GroundExpressionImpl(OperationPredicate functor, GroundTerm... terms) {
        super(functor, terms);
    }

    protected GroundExpressionImpl(OperationPredicate functor, ImmutableList<? extends GroundTerm> terms) {
        super(functor, terms);
    }

    protected GroundExpressionImpl(Expression expression) {
        super(expression);
        if (!GroundTermTools.isGroundTerm(expression)) {
            throw new IllegalArgumentException("Non-ground boolean expression given to build a ground expression!");
        }
    }

    @Override
    public ImmutableList<? extends GroundTerm> getArguments() {
        return (ImmutableList<? extends GroundTerm>)super.getArguments();
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
