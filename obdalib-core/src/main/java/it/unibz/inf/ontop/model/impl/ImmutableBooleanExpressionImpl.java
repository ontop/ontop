package it.unibz.inf.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;

public abstract class ImmutableBooleanExpressionImpl extends ImmutableFunctionalTermImpl implements ImmutableBooleanExpression {
    protected ImmutableBooleanExpressionImpl(OperationPredicate functor, ImmutableTerm... terms) {
        super(functor, terms);
    }

    protected ImmutableBooleanExpressionImpl(OperationPredicate functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor, terms);
    }

    protected ImmutableBooleanExpressionImpl(BooleanExpression expression) {
        super(expression);
    }

    @Override
    public ImmutableBooleanExpression clone() {
        return this;
    }

    @Override
    public OperationPredicate getFunctionSymbol() {
        return (OperationPredicate) super.getFunctionSymbol();
    }

    /**
     * Recursive
     */
    @Override
    public ImmutableSet<ImmutableBooleanExpression> flattenAND() {
        return flatten(ExpressionOperation.AND);
    }

    @Override
    public ImmutableSet<ImmutableBooleanExpression> flattenOR() {
        return flatten(ExpressionOperation.OR);
    }

    @Override
    public ImmutableSet<ImmutableBooleanExpression> flatten(OperationPredicate operator) {

        /**
         * Only flattens OR expressions.
         */
        if (getFunctionSymbol().equals(operator)) {
            ImmutableSet.Builder<ImmutableBooleanExpression> setBuilder = ImmutableSet.builder();
            for (ImmutableTerm subTerm : getArguments()) {
                /**
                 * Recursive call
                 */
                if (subTerm instanceof ImmutableBooleanExpression) {
                    setBuilder.addAll(((ImmutableBooleanExpression) subTerm).flatten(operator));
                }
                else {
                    throw new IllegalStateException("An AND-expression must be only composed of " +
                            "ImmutableBooleanExpression(s), not of a " + subTerm);
                }
            }
            return setBuilder.build();
        }
        else {
            return ImmutableSet.of((ImmutableBooleanExpression)this);
        }
    }
}
