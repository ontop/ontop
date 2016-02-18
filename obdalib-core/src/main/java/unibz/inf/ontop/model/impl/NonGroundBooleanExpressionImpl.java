package unibz.inf.ontop.model.impl;


import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.model.BooleanExpression;
import unibz.inf.ontop.model.BooleanOperationPredicate;
import unibz.inf.ontop.model.ImmutableTerm;
import unibz.inf.ontop.model.NonGroundFunctionalTerm;

import static unibz.inf.ontop.model.impl.GroundTermTools.checkNonGroundTermConstraint;

public class NonGroundBooleanExpressionImpl extends ImmutableBooleanExpressionImpl implements NonGroundFunctionalTerm {

    protected NonGroundBooleanExpressionImpl(BooleanOperationPredicate functor, ImmutableTerm... terms) {
        super(functor, terms);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundBooleanExpressionImpl(BooleanOperationPredicate functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor, terms);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundBooleanExpressionImpl(BooleanExpression expression) {
        super(expression);
        checkNonGroundTermConstraint(this);
    }

    @Override
    public boolean isGround() {
        return false;
    }
}
