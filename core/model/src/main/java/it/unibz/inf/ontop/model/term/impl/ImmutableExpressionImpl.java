package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

import javax.annotation.Nonnull;
import java.util.Optional;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.AND;
import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.OR;

public abstract class ImmutableExpressionImpl extends ImmutableFunctionalTermImpl implements ImmutableExpression {
    protected ImmutableExpressionImpl(TermFactory termFactory, BooleanFunctionSymbol functor, ImmutableTerm... terms) {
        super(functor, termFactory, terms);
    }

    protected ImmutableExpressionImpl(BooleanFunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms,
                                      TermFactory termFactory) {
        super(functor, terms, termFactory);
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

    @Override
    public Evaluation evaluate(TermFactory termFactory) {
        // NB: isInConstructionNodeInOptimizationPhase is irrelevant for expressions
        ImmutableTerm newTerm = simplify(false);
        if (newTerm instanceof ImmutableExpression)
            termFactory.getEvaluation((ImmutableExpression) newTerm);
        else if (newTerm.equals(termFactory.getDBBooleanConstant(true)))
            return termFactory.getPositiveEvaluation();
        else if (newTerm.equals(termFactory.getDBBooleanConstant(false)))
            return termFactory.getNegativeEvaluation();
        else if (newTerm.equals(termFactory.getNullConstant()))
            return termFactory.getNullEvaluation();

        throw new IncorrectExpressionSimplificationBugException(this, newTerm);
    }


    protected static class ExpressionEvaluationImpl implements ImmutableExpression.Evaluation {
        @Nonnull
        private final ImmutableExpression expression;

        protected ExpressionEvaluationImpl(@Nonnull ImmutableExpression expression) {
            this.expression = expression;
        }

        @Override
        public Optional<ImmutableExpression> getExpression() {
            return Optional.of(expression);
        }

        @Override
        public Optional<BooleanValue> getValue() {
            return Optional.empty();
        }

        @Override
        public ImmutableTerm getTerm() {
            return expression;
        }
    }

    protected static class ValueEvaluationImpl implements ImmutableExpression.Evaluation {

        private final BooleanValue value;
        private final Constant constant;

        protected ValueEvaluationImpl(BooleanValue value, Constant constant) {
            this.value = value;
            this.constant = constant;
        }

        @Override
        public Optional<ImmutableExpression> getExpression() {
            return Optional.empty();
        }

        @Override
        public Optional<BooleanValue> getValue() {
            return Optional.of(value);
        }

        @Override
        public ImmutableTerm getTerm() {
            return constant;
        }
    }

    private static class IncorrectExpressionSimplificationBugException extends OntopInternalBugException {

        protected IncorrectExpressionSimplificationBugException(ImmutableExpression expression,
                                                                ImmutableTerm resultingTerm) {
            super(String.format("Incorrect simplication of %s: led to %s", expression, resultingTerm));
        }
    }

}
