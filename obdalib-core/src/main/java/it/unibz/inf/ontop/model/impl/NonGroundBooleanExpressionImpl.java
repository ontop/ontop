package it.unibz.inf.ontop.model.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.BooleanExpression;
import it.unibz.inf.ontop.model.OperationPredicate;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.NonGroundFunctionalTerm;

import static it.unibz.inf.ontop.model.impl.GroundTermTools.checkNonGroundTermConstraint;

public class NonGroundBooleanExpressionImpl extends ImmutableBooleanExpressionImpl implements NonGroundFunctionalTerm {

    protected NonGroundBooleanExpressionImpl(OperationPredicate functor, ImmutableTerm... terms) {
        super(functor, terms);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundBooleanExpressionImpl(OperationPredicate functor, ImmutableList<? extends ImmutableTerm> terms) {
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
