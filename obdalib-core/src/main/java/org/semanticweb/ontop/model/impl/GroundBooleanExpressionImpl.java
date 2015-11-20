package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.*;

public class GroundBooleanExpressionImpl extends ImmutableBooleanExpressionImpl implements GroundFunctionalTerm {

    protected GroundBooleanExpressionImpl(BooleanOperationPredicate functor, GroundTerm... terms) {
        super(functor, terms);
    }

    protected GroundBooleanExpressionImpl(BooleanOperationPredicate functor, ImmutableList<? extends GroundTerm> terms) {
        super(functor, terms);
    }

    protected GroundBooleanExpressionImpl(BooleanExpression expression) {
        super(expression);
        if (!GroundTermTools.isGroundTerm(expression)) {
            throw new IllegalArgumentException("Non-ground boolean expression given to build a ground expression!");
        }
    }

    @Override
    public ImmutableList<GroundTerm> getGroundTerms() {
        return (ImmutableList<GroundTerm>)(ImmutableList<?>) getImmutableTerms();
    }

    @Override
    public boolean isGround() {
        return true;
    }
}
