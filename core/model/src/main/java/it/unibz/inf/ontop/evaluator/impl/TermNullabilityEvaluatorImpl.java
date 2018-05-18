package it.unibz.inf.ontop.evaluator.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator.EvaluationResult;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


@Singleton
public class TermNullabilityEvaluatorImpl implements TermNullabilityEvaluator {

    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final DatalogTools datalogTools;
    private final ValueConstant valueNull;
    private final ExpressionEvaluator defaultExpressionEvaluator;

    @Inject
    private TermNullabilityEvaluatorImpl(SubstitutionFactory substitutionFactory, TermFactory termFactory,
                                         TypeFactory typeFactory, DatalogTools datalogTools,
                                         ExpressionEvaluator defaultExpressionEvaluator) {
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.datalogTools = datalogTools;
        this.valueNull = termFactory.getNullConstant();
        this.defaultExpressionEvaluator = defaultExpressionEvaluator;
    }

    @Override
    public boolean isNullable(ImmutableTerm term, ImmutableSet<Variable> nullableVariables) {

        if (term instanceof ImmutableFunctionalTerm) {
            return isFunctionalTermNullable((ImmutableFunctionalTerm) term, nullableVariables);
        }
        else if (term instanceof Constant) {
            return term.equals(valueNull);
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
        ImmutableExpression nullCaseExpression = substitutionFactory.getSubstitution(variable, valueNull)
                .applyToBooleanExpression(expression);

        EvaluationResult evaluationResult = defaultExpressionEvaluator.clone()
                .evaluateExpression(nullCaseExpression);
        return evaluationResult.isEffectiveFalse();
    }

    @Override
    public boolean isFilteringNullValues(ImmutableExpression expression, ImmutableSet<Variable> tightVariables) {
        ImmutableExpression nullCaseExpression = substitutionFactory.getSubstitution(
                tightVariables.stream()
                        .collect(ImmutableCollectors.toMap(
                                v -> v,
                                v -> valueNull)))
                .applyToBooleanExpression(expression);

        EvaluationResult evaluationResult = defaultExpressionEvaluator.clone()
                .evaluateExpression(nullCaseExpression);
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
        return functionalTerm.getTerms().stream()
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
