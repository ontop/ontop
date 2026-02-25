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

import java.util.Map;
import java.util.stream.Stream;


@Singleton
public class TermNullabilityEvaluatorImpl implements TermNullabilityEvaluator {

    private static final long TERM_EVALUATOR_CACHE_SIZE = 10000;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final CoreUtilsFactory coreUtilsFactory;

    private final Cache<Map.Entry<ImmutableExpression, ImmutableSet<Variable>>, Boolean> cache;

    @Inject
    private TermNullabilityEvaluatorImpl(TermFactory termFactory, SubstitutionFactory substitutionFactory, CoreUtilsFactory coreUtilsFactory) {
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(TERM_EVALUATOR_CACHE_SIZE)
                .build();
    }

    @Override
    public boolean isFilteringNullValue(ImmutableExpression expression, Variable variable) {
        ImmutableExpression nullCaseExpression =
                substitutionFactory.getSubstitution(variable,  termFactory.getNullConstant())
                .apply(expression);

        return nullCaseExpression.evaluate2VL(coreUtilsFactory.createSimplifiedVariableNullability(expression))
                .isEffectiveFalse();
    }

    @Override
    public boolean isFilteringNullValues(ImmutableExpression expression, ImmutableSet<Variable> tightVariables) {
        Map.Entry<ImmutableExpression, ImmutableSet<Variable>> entry = Maps.immutableEntry(expression, tightVariables);

        Boolean cacheResult = cache.getIfPresent(entry);
        if (cacheResult != null)
            return cacheResult;

        ImmutableExpression nullCaseExpression = tightVariables.stream()
                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant()))
                .apply(expression);

        boolean result = nullCaseExpression.evaluate2VL(coreUtilsFactory.createSimplifiedVariableNullability(expression))
                .isEffectiveFalse();
        cache.put(entry, result);
        return result;
    }
}
