package it.unibz.inf.ontop.model.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.BooleanExpression;
import it.unibz.inf.ontop.model.BooleanOperationPredicate;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.NonGroundFunctionalTerm;

public class NonGroundBooleanExpressionImpl extends ImmutableBooleanExpressionImpl implements NonGroundFunctionalTerm {

    protected NonGroundBooleanExpressionImpl(BooleanOperationPredicate functor, ImmutableTerm... terms) {
        super(functor, terms);
        GroundTermTools.checkNonGroundTermConstraint(this);
    }

    protected NonGroundBooleanExpressionImpl(BooleanOperationPredicate functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor, terms);
        GroundTermTools.checkNonGroundTermConstraint(this);
    }

    protected NonGroundBooleanExpressionImpl(BooleanExpression expression) {
        super(expression);
        GroundTermTools.checkNonGroundTermConstraint(this);
    }

    @Override
    public boolean isGround() {
        return false;
    }
}
