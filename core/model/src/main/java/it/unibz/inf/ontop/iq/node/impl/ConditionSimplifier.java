package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;

public class ConditionSimplifier {

    private final SubstitutionFactory substitutionFactory;
    private final ImmutabilityTools immutabilityTools;
    private final TermFactory termFactory;
    private final ImmutableUnificationTools unificationTools;
    private final ImmutableSubstitutionTools substitutionTools;
    private final ExpressionEvaluator defaultExpressionEvaluator;

    @Inject
    private ConditionSimplifier(SubstitutionFactory substitutionFactory, ImmutabilityTools immutabilityTools,
                                TermFactory termFactory, ImmutableUnificationTools unificationTools,
                                ImmutableSubstitutionTools substitutionTools,
                                ExpressionEvaluator defaultExpressionEvaluator) {
        this.substitutionFactory = substitutionFactory;
        this.immutabilityTools = immutabilityTools;
        this.termFactory = termFactory;
        this.unificationTools = unificationTools;
        this.substitutionTools = substitutionTools;
        this.defaultExpressionEvaluator = defaultExpressionEvaluator;
    }

    public ExpressionAndSubstitution simplifyCondition(ImmutableExpression expression)
            throws UnsatisfiableConditionException{
        return simplifyCondition(Optional.of(expression), ImmutableSet.of());
    }

    public ExpressionAndSubstitution simplifyCondition(Optional<ImmutableExpression> nonOptimizedExpression,
                                                       ImmutableSet<Variable> nonLiftableVariables)
            throws UnsatisfiableConditionException {

        Optional<ExpressionEvaluator.EvaluationResult> optionalEvaluationResults =
                nonOptimizedExpression
                        .map(this::evaluateExpression);

        if (optionalEvaluationResults.isPresent()) {
            ExpressionEvaluator.EvaluationResult results = optionalEvaluationResults.get();

            if (results.isEffectiveFalse())
                throw new UnsatisfiableConditionException();

            Optional<ImmutableExpression> optionalExpression = results.getOptionalExpression();
            if (optionalExpression.isPresent())
                // May throw an exception if unification is rejected
                return convertIntoExpressionAndSubstitution(optionalExpression.get(), nonLiftableVariables);
            else
                return new ExpressionAndSubstitution(Optional.empty(), substitutionFactory.getSubstitution());
        }
        else
            return new ExpressionAndSubstitution(Optional.empty(), substitutionFactory.getSubstitution());
    }

    private ExpressionEvaluator.EvaluationResult evaluateExpression(ImmutableExpression expression) {
        return (defaultExpressionEvaluator.clone()).evaluateExpression(expression);
    }

    /**
     * TODO: explain
     *
     * Functional terms remain in the expression (never going into the substitution)
     *
     */
    private ExpressionAndSubstitution convertIntoExpressionAndSubstitution(ImmutableExpression expression,
                                                                                                ImmutableSet<Variable> nonLiftableVariables)
            throws UnsatisfiableConditionException {

        ImmutableSet<ImmutableExpression> expressions = expression.flattenAND();
        ImmutableSet<ImmutableExpression> functionFreeEqualities = expressions.stream()
                .filter(e -> e.getFunctionSymbol().equals(EQ))
                .filter(e -> {
                    ImmutableList<? extends ImmutableTerm> arguments = e.getArguments();
                    return arguments.stream().allMatch(t -> t instanceof NonFunctionalTerm);
                })
                .collect(ImmutableCollectors.toSet());

        ImmutableSubstitution<NonFunctionalTerm> normalizedUnifier = unify(functionFreeEqualities.stream()
                        .map(ImmutableFunctionalTerm::getArguments)
                        .map(args -> Maps.immutableEntry(
                                (NonFunctionalTerm) args.get(0),
                                (NonFunctionalTerm)args.get(1))),
                nonLiftableVariables);

        Optional<ImmutableExpression> newExpression = immutabilityTools.foldBooleanExpressions(
                Stream.concat(
                        // Expressions that are not function-free equalities
                        expressions.stream()
                                .filter(e -> !functionFreeEqualities.contains(e))
                                .map(normalizedUnifier::applyToBooleanExpression),

                        // Equalities that must remain
                        normalizedUnifier.getImmutableMap().entrySet().stream()
                                .filter(e -> nonLiftableVariables.contains(e.getKey()))
                                .map(e -> termFactory.getImmutableExpression(EQ, e.getKey(), e.getValue()))
                ));

        return new ExpressionAndSubstitution(newExpression, normalizedUnifier);
    }

    private ImmutableSubstitution<NonFunctionalTerm> unify(
            Stream<Map.Entry<NonFunctionalTerm, NonFunctionalTerm>> equalityStream,
            ImmutableSet<Variable> nonLiftableVariables) throws UnsatisfiableConditionException {
        ImmutableList<Map.Entry<NonFunctionalTerm, NonFunctionalTerm>> equalities = equalityStream.collect(ImmutableCollectors.toList());

        ImmutableList<NonFunctionalTerm> args1 = equalities.stream()
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toList());

        ImmutableList<NonFunctionalTerm> args2 = equalities.stream()
                .map(Map.Entry::getValue)
                .collect(ImmutableCollectors.toList());

        return unificationTools.computeMGU(args1, args2)
                // TODO: merge priorityRenaming with the orientate() method
                .map(u -> substitutionTools.prioritizeRenaming(u, nonLiftableVariables))
                .orElseThrow(UnsatisfiableConditionException::new);
    }


    protected static class ExpressionAndSubstitution {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public final Optional<ImmutableExpression> optionalExpression;
        public final ImmutableSubstitution<NonFunctionalTerm> substitution;

        protected ExpressionAndSubstitution(Optional<ImmutableExpression> optionalExpression,
                                            ImmutableSubstitution<NonFunctionalTerm> substitution) {
            this.optionalExpression = optionalExpression;
            this.substitution = substitution;
        }
    }

}
