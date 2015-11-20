package org.semanticweb.ontop.model.impl;


import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.model.BooleanOperationPredicate;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.NonGroundFunctionalTerm;

import static org.semanticweb.ontop.model.impl.GroundTermTools.checkNonGroundTermConstraint;

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
