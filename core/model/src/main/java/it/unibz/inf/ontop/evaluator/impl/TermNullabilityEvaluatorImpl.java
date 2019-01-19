package it.unibz.inf.ontop.evaluator.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator.EvaluationResult;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


@Singleton
public class TermNullabilityEvaluatorImpl implements TermNullabilityEvaluator {

    private final SubstitutionFactory substitutionFactory;
    private final Constant valueNull;
    private final ExpressionEvaluator defaultExpressionEvaluator;
    private final CoreUtilsFactory coreUtilsFactory;

    @Inject
    private TermNullabilityEvaluatorImpl(SubstitutionFactory substitutionFactory, TermFactory termFactory,
                                         ExpressionEvaluator defaultExpressionEvaluator,
                                         CoreUtilsFactory coreUtilsFactory) {
        this.substitutionFactory = substitutionFactory;
        this.valueNull = termFactory.getNullConstant();
        this.defaultExpressionEvaluator = defaultExpressionEvaluator;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public boolean isFilteringNullValue(ImmutableExpression expression, Variable variable) {
        ImmutableExpression nullCaseExpression = substitutionFactory.getSubstitution(variable, valueNull)
                .applyToBooleanExpression(expression);

        EvaluationResult evaluationResult = defaultExpressionEvaluator.clone()
                .evaluateExpression(nullCaseExpression, coreUtilsFactory.createDummyVariableNullability(expression));
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
                .evaluateExpression(nullCaseExpression, coreUtilsFactory.createDummyVariableNullability(expression));
        return evaluationResult.isEffectiveFalse();
    }
}
