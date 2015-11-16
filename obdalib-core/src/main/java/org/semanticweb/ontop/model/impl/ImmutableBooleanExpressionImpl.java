package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.*;

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

    /**
     * Recursive
     */
    @Override
    public ImmutableList<ImmutableBooleanExpression> flatten() {

        Predicate functionSymbol = getFunctionSymbol();
        /**
         * Only flattens AND expressions.
         */
        if (functionSymbol.equals(OBDAVocabulary.AND)) {
            ImmutableList.Builder<ImmutableBooleanExpression> listBuilder = ImmutableList.builder();
            for (ImmutableTerm subTerm : getImmutableTerms()) {
                /**
                 * Recursive call
                 */
                if (subTerm instanceof ImmutableBooleanExpression) {
                    listBuilder.addAll(((ImmutableBooleanExpression)subTerm).flatten());
                }
            }
            return listBuilder.build();
        }
        else {
            return ImmutableList.of((ImmutableBooleanExpression)this);
        }
    }
}
