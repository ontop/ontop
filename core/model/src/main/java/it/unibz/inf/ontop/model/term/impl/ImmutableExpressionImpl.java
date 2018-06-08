package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.AND;
import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.OR;

public abstract class ImmutableExpressionImpl extends ImmutableFunctionalTermImpl implements ImmutableExpression {
    protected ImmutableExpressionImpl(BooleanFunctionSymbol functor, ImmutableTerm... terms) {
        super(functor, terms);
    }

    protected ImmutableExpressionImpl(BooleanFunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor, terms);
    }

    @Override
    public ImmutableExpressionImpl clone() {
        return this;
    }

    @Override
    public BooleanFunctionSymbol getFunctionSymbol() {
        return (BooleanFunctionSymbol) super.getFunctionSymbol();
    }

    /**
     * Recursive
     */
    @Override
    public ImmutableSet<ImmutableExpression> flattenAND() {
        return flatten(AND);
    }

    @Override
    public ImmutableSet<ImmutableExpression> flattenOR() {
        return flatten(OR);
    }

    @Override
    public ImmutableSet<ImmutableExpression> flatten(BooleanFunctionSymbol operator) {

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
