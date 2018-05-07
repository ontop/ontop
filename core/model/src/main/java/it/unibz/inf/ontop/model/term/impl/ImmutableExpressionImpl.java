package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

public abstract class ImmutableExpressionImpl extends ImmutableFunctionalTermImpl implements ImmutableExpression {
    protected ImmutableExpressionImpl(OperationPredicate functor, ImmutableTerm... terms) {
        super(functor, terms);
    }

    protected ImmutableExpressionImpl(OperationPredicate functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor, terms);
    }

    @Override
    public ImmutableExpressionImpl clone() {
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
    public ImmutableSet<ImmutableExpression> flattenAND() {
        return flatten(ExpressionOperation.AND);
    }

    @Override
    public ImmutableSet<ImmutableExpression> flattenOR() {
        return flatten(ExpressionOperation.OR);
    }

    @Override
    public ImmutableSet<ImmutableExpression> flatten(OperationPredicate operator) {

        /**
         * Only flattens OR expressions.
         */
        if (getFunctionSymbol().equals(operator)) {
            ImmutableSet.Builder<ImmutableExpression> setBuilder = ImmutableSet.builder();
            for (ImmutableTerm subTerm : getTerms()) {
                /**
                 * Recursive call
                 */
                if (subTerm instanceof ImmutableExpression) {
                    setBuilder.addAll(((ImmutableExpression) subTerm).flatten(operator));
                }
                else {
                    throw new IllegalStateException("An AND-expression must be only composed of " +
                            "ImmutableBooleanExpression(s), not of a " + subTerm);
                }
            }
            return setBuilder.build();
        }
        else {
            return ImmutableSet.of(this);
        }
    }
}
