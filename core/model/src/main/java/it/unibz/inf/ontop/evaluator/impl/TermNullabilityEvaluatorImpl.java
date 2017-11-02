package it.unibz.inf.ontop.evaluator.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator.EvaluationResult;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import static it.unibz.inf.ontop.model.term.TermConstants.NULL;


@Singleton
public class TermNullabilityEvaluatorImpl implements TermNullabilityEvaluator {

    private final SubstitutionFactory substitutionFactory;

    @Inject
    private TermNullabilityEvaluatorImpl(SubstitutionFactory substitutionFactory) {
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public boolean isNullable(ImmutableTerm term, ImmutableSet<Variable> nullableVariables) {

        if (term instanceof ImmutableFunctionalTerm) {
            return isFunctionalTermNullable((ImmutableFunctionalTerm) term, nullableVariables);
        }
        else if (term instanceof Constant) {
            return term.equals(NULL);
        }
        else if (term instanceof Variable) {
            return nullableVariables.contains(term);
        }
        else {
            throw new IllegalStateException("Unexpected immutable term");
        }
    }

    @Override
    public boolean isFilteringNullValue(ImmutableExpression expression, Variable variable) {
        ImmutableExpression nullCaseExpression = substitutionFactory.getSubstitution(variable, NULL)
                .applyToBooleanExpression(expression);

        // TODO: inject the expression evaluator instead
        EvaluationResult evaluationResult = new ExpressionEvaluator().evaluateExpression(nullCaseExpression);
        return evaluationResult.isEffectiveFalse();
    }

    private boolean isFunctionalTermNullable(ImmutableFunctionalTerm functionalTerm,
                                             ImmutableSet<Variable> nullableVariables) {
        if (functionalTerm instanceof ImmutableExpression) {
            return isExpressionNullable((ImmutableExpression)functionalTerm, nullableVariables);
        }
        else {
            return hasNullableArgument(functionalTerm, nullableVariables);
        }
    }

    private boolean hasNullableArgument(ImmutableFunctionalTerm functionalTerm, ImmutableSet<Variable> nullableVariables) {
        return functionalTerm.getArguments().stream()
                .anyMatch(t -> isNullable(t, nullableVariables));
    }

    private boolean isExpressionNullable(ImmutableExpression expression, ImmutableSet<Variable> nullableVariables) {
        OperationPredicate functionSymbol = expression.getFunctionSymbol();

        if (functionSymbol instanceof ExpressionOperation) {
            switch((ExpressionOperation) functionSymbol) {
                case IS_NOT_NULL:
                case IS_NULL:
                    return false;
                default:
                    break;
            }
        }
        // TODO: support COALESCE and IF-THEN-ELSE (they will need to use isFilteringNullValue)

        return hasNullableArgument(expression, nullableVariables);
    }
}
