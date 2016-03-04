package it.unibz.inf.ontop.model.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Expression;
import it.unibz.inf.ontop.model.OperationPredicate;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.NonGroundFunctionalTerm;

import static it.unibz.inf.ontop.model.impl.GroundTermTools.checkNonGroundTermConstraint;

public class NonGroundExpressionImpl extends ImmutableExpressionImpl implements NonGroundFunctionalTerm {

    protected NonGroundExpressionImpl(OperationPredicate functor, ImmutableTerm... terms) {
        super(functor, terms);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundExpressionImpl(OperationPredicate functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor, terms);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundExpressionImpl(Expression expression) {
        super(expression);
        checkNonGroundTermConstraint(this);
    }

    @Override
    public boolean isGround() {
        return false;
    }
}
