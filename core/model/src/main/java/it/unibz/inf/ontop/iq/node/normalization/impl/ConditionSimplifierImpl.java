package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
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

@Singleton
public class ConditionSimplifierImpl implements ConditionSimplifier {

    private final SubstitutionFactory substitutionFactory;
    private final ImmutabilityTools immutabilityTools;
    private final TermFactory termFactory;
    private final ImmutableUnificationTools unificationTools;
    private final ImmutableSubstitutionTools substitutionTools;
    private final ExpressionEvaluator defaultExpressionEvaluator;

    @Inject
    private ConditionSimplifierImpl(SubstitutionFactory substitutionFactory, ImmutabilityTools immutabilityTools,
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


    @Override
    public ExpressionAndSubstitution simplifyCondition(ImmutableExpression expression)
            throws UnsatisfiableConditionException {
        return simplifyCondition(Optional.of(expression), ImmutableSet.of());
    }

    @Override
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
                return new ExpressionAndSubstitutionImpl(Optional.empty(), substitutionFactory.getSubstitution());
        }
        else
            return new ExpressionAndSubstitutionImpl(Optional.empty(), substitutionFactory.getSubstitution());
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
                    ImmutableList<? extends ImmutableTerm> arguments = e.getTerms();
                    return arguments.stream().allMatch(t -> t instanceof NonFunctionalTerm);
                })
                .collect(ImmutableCollectors.toSet());

        ImmutableSubstitution<NonFunctionalTerm> normalizedUnifier = unify(functionFreeEqualities.stream()
                        .map(ImmutableFunctionalTerm::getTerms)
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

        return new ExpressionAndSubstitutionImpl(newExpression, normalizedUnifier);
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

    @Override
    public Optional<ImmutableExpression> computeDownConstraint(Optional<ImmutableExpression> optionalConstraint,
                                                               ExpressionAndSubstitution conditionSimplificationResults)
            throws UnsatisfiableConditionException {
        if (optionalConstraint.isPresent()) {
            ImmutableExpression substitutedConstraint = conditionSimplificationResults.getSubstitution()
                    .applyToBooleanExpression(optionalConstraint.get());

            ImmutableExpression combinedExpression = conditionSimplificationResults.getOptionalExpression()
                    .flatMap(e -> immutabilityTools.foldBooleanExpressions(
                            Stream.concat(
                                    e.flattenAND().stream(),
                                    substitutedConstraint.flattenAND().stream())))
                    .orElse(substitutedConstraint);

            ExpressionEvaluator.EvaluationResult evaluationResults = evaluateExpression(combinedExpression);

            if (evaluationResults.isEffectiveFalse())
                throw new UnsatisfiableConditionException();

            return evaluationResults.getOptionalExpression();
        }
        else
            return conditionSimplificationResults.getOptionalExpression();
    }


}
