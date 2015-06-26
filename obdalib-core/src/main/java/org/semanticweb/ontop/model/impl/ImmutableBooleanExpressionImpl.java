package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.model.BooleanOperationPredicate;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.model.ImmutableTerm;

public class ImmutableBooleanExpressionImpl extends ImmutableFunctionalTermImpl implements ImmutableBooleanExpression {
    protected ImmutableBooleanExpressionImpl(BooleanOperationPredicate functor, ImmutableTerm... terms) {
        super(functor, terms);
    }

    protected ImmutableBooleanExpressionImpl(BooleanOperationPredicate functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor, terms);
    }

    public ImmutableBooleanExpressionImpl(BooleanExpression expression) {
        super(expression);
    }

    @Override
    public ImmutableBooleanExpression clone() {
        return this;
    }
}
