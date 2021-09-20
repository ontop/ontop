package it.unibz.inf.ontop.evaluator.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;


@Singleton
public class TermNullabilityEvaluatorImpl implements TermNullabilityEvaluator {

    private static final long TERM_EVALUATOR_CACHE_SIZE = 10000;
    private final SubstitutionFactory substitutionFactory;
    private final Constant valueNull;
    private final CoreUtilsFactory coreUtilsFactory;

    // Made stable in more recent versions of Guava (NB: we cannot update because of Protégé)
    @SuppressWarnings("UnstableApiUsage")
    private final Cache<Map.Entry<ImmutableExpression, ImmutableSet<Variable>>, Boolean> cache;

    @Inject
    private TermNullabilityEvaluatorImpl(SubstitutionFactory substitutionFactory, TermFactory termFactory,
                                         CoreUtilsFactory coreUtilsFactory) {
        this.substitutionFactory = substitutionFactory;
        this.valueNull = termFactory.getNullConstant();
        this.coreUtilsFactory = coreUtilsFactory;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(TERM_EVALUATOR_CACHE_SIZE)
                .build();
    }

    @Override
    public boolean isFilteringNullValue(ImmutableExpression expression, Variable variable) {
        ImmutableExpression nullCaseExpression = substitutionFactory.getSubstitution(variable, valueNull)
                .applyToBooleanExpression(expression);

        return nullCaseExpression.evaluate2VL(coreUtilsFactory.createSimplifiedVariableNullability(expression))
                .isEffectiveFalse();
    }

    @Override
    public boolean isFilteringNullValues(ImmutableExpression expression, ImmutableSet<Variable> tightVariables) {
        Map.Entry<ImmutableExpression, ImmutableSet<Variable>> entry = Maps.immutableEntry(expression, tightVariables);

        Boolean cacheResult = cache.getIfPresent(entry);
        if (cacheResult != null)
            return cacheResult;

        ImmutableExpression nullCaseExpression = substitutionFactory.getSubstitution(
                tightVariables.stream()
                        .collect(ImmutableCollectors.toMap(
                                v -> v,
                                v -> valueNull)))
                .applyToBooleanExpression(expression);

        boolean result = nullCaseExpression.evaluate2VL(coreUtilsFactory.createSimplifiedVariableNullability(expression))
                .isEffectiveFalse();
        cache.put(entry, result);
        return result;
    }
}
