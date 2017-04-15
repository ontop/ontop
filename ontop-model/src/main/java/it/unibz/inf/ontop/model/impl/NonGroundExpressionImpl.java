package it.unibz.inf.ontop.model.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;

import static it.unibz.inf.ontop.model.ExpressionOperation.EQ;
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

    @Override
    public boolean isVar2VarEquality() {
        return getFunctionSymbol().equals(EQ) &&
                getTerms().size() == 2 &&
                getTerms().stream().allMatch(t -> t instanceof Variable);
    }
}
