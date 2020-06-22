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
    public BooleanFunctionSymbol getFunctionSymbol() {
        return (BooleanFunctionSymbol) super.getFunctionSymbol();
    }

    /**
     * Recursive
     */
    @Override
    public Stream<ImmutableExpression> flattenAND() {
        if (super.getFunctionSymbol() instanceof DBAndFunctionSymbol) {
            return getTerms().stream()
                    .map(t -> (ImmutableExpression) t)
                    .flatMap(ImmutableExpression::flattenAND)
                    .distinct();
        }
        return Stream.of(this);
    }

    @Override
    public Stream<ImmutableExpression> flattenOR() {
        if (super.getFunctionSymbol() instanceof DBOrFunctionSymbol) {
            return getTerms().stream()
                    .map(t -> (ImmutableExpression) t)
                    .flatMap(ImmutableExpression::flattenOR)
                    .distinct();
        }
        return Stream.of(this);
    }

    @Override
    public Evaluation evaluate(VariableNullability variableNullability) {
        ImmutableTerm newTerm = simplify(variableNullability);
        return convertTermToEvaluation(newTerm, true);
    }

    @Override
    public Evaluation evaluate2VL(VariableNullability variableNullability) {
        ImmutableTerm newTerm = simplify2VL(variableNullability);
        return convertTermToEvaluation(newTerm, false);
    }

    private Evaluation convertTermToEvaluation(ImmutableTerm newTerm, boolean use3VL) {
        if (newTerm instanceof ImmutableExpression)
            return termFactory.getEvaluation((ImmutableExpression) newTerm);
        else if (newTerm.equals(termFactory.getDBBooleanConstant(true)))
            return termFactory.getPositiveEvaluation();
        else if (newTerm.equals(termFactory.getDBBooleanConstant(false)))
            return termFactory.getNegativeEvaluation();
        else if (newTerm.equals(termFactory.getNullConstant()))
            return use3VL ? termFactory.getNullEvaluation() : termFactory.getNegativeEvaluation();
        else if (newTerm instanceof NonFunctionalTerm)
            return termFactory.getEvaluation(termFactory.getIsTrue((NonFunctionalTerm) newTerm));

        throw new IncorrectExpressionSimplificationBugException(this, newTerm);
    }

    @Override
    public IncrementalEvaluation evaluate(VariableNullability variableNullability, boolean isExpressionNew) {
        return evaluate(variableNullability)
                .getEvaluationResult(this, isExpressionNew);
    }

    @Override
    public IncrementalEvaluation evaluate2VL(VariableNullability variableNullability, boolean isExpressionNew) {
        return evaluate2VL(variableNullability)
                .getEvaluationResult(this, isExpressionNew);
    }

    @Override
    public ImmutableTerm simplify2VL(VariableNullability variableNullability) {
        return getFunctionSymbol().simplify2VL(getTerms(), termFactory, variableNullability);
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
        public IncrementalEvaluation getEvaluationResult(ImmutableExpression originalExpression,
                                                         boolean wasExpressionAlreadyNew) {
            return (wasExpressionAlreadyNew || (!originalExpression.equals(expression)))
                    ? IncrementalEvaluation.declareSimplifiedExpression(expression)
                    : IncrementalEvaluation.declareSameExpression();
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
        public IncrementalEvaluation getEvaluationResult(ImmutableExpression originalExpression,
                                                         boolean wasExpressionAlreadyNew) {
            switch(value) {
                case TRUE:
                    return IncrementalEvaluation.declareIsTrue();
                case FALSE:
                    return IncrementalEvaluation.declareIsFalse();
                // NULL
                default:
                    return IncrementalEvaluation.declareIsNull();
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
