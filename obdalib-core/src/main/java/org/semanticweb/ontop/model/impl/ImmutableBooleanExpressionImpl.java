package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;

public abstract class ImmutableBooleanExpressionImpl extends ImmutableFunctionalTermImpl implements ImmutableBooleanExpression {
    protected ImmutableBooleanExpressionImpl(BooleanOperationPredicate functor, ImmutableTerm... terms) {
        super(functor, terms);
    }

    protected ImmutableBooleanExpressionImpl(BooleanOperationPredicate functor, ImmutableList<? extends ImmutableTerm> terms) {
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
    public BooleanOperationPredicate getFunctionSymbol() {
        return (BooleanOperationPredicate) super.getFunctionSymbol();
    }

    /**
     * Recursive
     */
    @Override
    public ImmutableSet<ImmutableBooleanExpression> flatten() {

        Predicate functionSymbol = getFunctionSymbol();
        /**
         * Only flattens AND expressions.
         */
        if (functionSymbol.equals(OBDAVocabulary.AND)) {
            ImmutableSet.Builder<ImmutableBooleanExpression> setBuilder = ImmutableSet.builder();
            for (ImmutableTerm subTerm : getArguments()) {
                /**
                 * Recursive call
                 */
                if (subTerm instanceof ImmutableBooleanExpression) {
                    setBuilder.addAll(((ImmutableBooleanExpression) subTerm).flatten());
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
