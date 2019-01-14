package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBAndFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Stream;

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
    public Stream<ImmutableExpression> flattenAND() {
        if (getFunctionSymbol() instanceof DBAndFunctionSymbol) {
            return getTerms().stream()
                    .map(t -> (ImmutableExpression) t)
                    .distinct();
        }
        return Stream.of(this);
    }

    @Override
    public Stream<ImmutableExpression> flattenOR() {
        if (getFunctionSymbol() instanceof DBOrFunctionSymbol) {
            return getTerms().stream()
                    .map(t -> (ImmutableExpression) t)
                    .distinct();
        }
        return Stream.of(this);
    }

    @Override
    public Evaluation evaluate(TermFactory termFactory, VariableNullability variableNullability) {
        // NB: isInConstructionNodeInOptimizationPhase is irrelevant for expressions
        ImmutableTerm newTerm = simplify(false, variableNullability);
        if (newTerm instanceof ImmutableExpression)
            return termFactory.getEvaluation((ImmutableExpression) newTerm);
        else if (newTerm.equals(termFactory.getDBBooleanConstant(true)))
            return termFactory.getPositiveEvaluation();
        else if (newTerm.equals(termFactory.getDBBooleanConstant(false)))
            return termFactory.getNegativeEvaluation();
        else if (newTerm.equals(termFactory.getNullConstant()))
            return termFactory.getNullEvaluation();

        throw new IncorrectExpressionSimplificationBugException(this, newTerm);
    }

    @Override
    public ImmutableExpression negate(TermFactory termFactory) {
        BooleanFunctionSymbol functionSymbol = getFunctionSymbol();

        if (functionSymbol.blocksNegation()) {
            return termFactory.getDBNot(this);
        }
        else
            return functionSymbol.negate(getTerms(), termFactory);
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

        @Override
        public EvaluationResult getEvaluationResult(ImmutableExpression originalExpression,
                                                    boolean wasExpressionAlreadyNew) {
            return (wasExpressionAlreadyNew || (!originalExpression.equals(expression)))
                    ? EvaluationResult.declareSimplifiedExpression(expression)
                    : EvaluationResult.declareSameExpression();
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

        @Override
        public EvaluationResult getEvaluationResult(ImmutableExpression originalExpression,
                                                    boolean wasExpressionAlreadyNew) {
            switch(value) {
                case TRUE:
                    return EvaluationResult.declareIsTrue();
                case FALSE:
                    return EvaluationResult.declareIsFalse();
                // NULL
                default:
                    return EvaluationResult.declareIsNull();
            }
        }
    }

    private static class IncorrectExpressionSimplificationBugException extends OntopInternalBugException {

        protected IncorrectExpressionSimplificationBugException(ImmutableExpression expression,
                                                                ImmutableTerm resultingTerm) {
            super(String.format("Incorrect simplication of %s: led to %s", expression, resultingTerm));
        }
    }

}
