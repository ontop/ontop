package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.node.JoinOrFilterNode;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;

public abstract class JoinOrFilterNodeImpl extends CompositeQueryNodeImpl implements JoinOrFilterNode {

    private Optional<ImmutableExpression> optionalFilterCondition;
    private final TermNullabilityEvaluator nullabilityEvaluator;
    protected final TermFactory termFactory;
    protected final TypeFactory typeFactory;
    protected final DatalogTools datalogTools;
    protected final ImmutabilityTools immutabilityTools;
    protected final SubstitutionFactory substitutionFactory;
    protected final ImmutableUnificationTools unificationTools;
    protected final ImmutableSubstitutionTools substitutionTools;
    private final ExpressionEvaluator defaultExpressionEvaluator;

    protected JoinOrFilterNodeImpl(Optional<ImmutableExpression> optionalFilterCondition,
                                   TermNullabilityEvaluator nullabilityEvaluator, TermFactory termFactory,
                                   IntermediateQueryFactory iqFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                                   ImmutabilityTools immutabilityTools, SubstitutionFactory substitutionFactory,
                                   ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools,
                                   ExpressionEvaluator defaultExpressionEvaluator) {
        super(substitutionFactory, iqFactory);
        this.optionalFilterCondition = optionalFilterCondition;
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.datalogTools = datalogTools;
        this.immutabilityTools = immutabilityTools;
        this.substitutionFactory = substitutionFactory;
        this.unificationTools = unificationTools;
        this.substitutionTools = substitutionTools;
        this.defaultExpressionEvaluator = defaultExpressionEvaluator;
    }

    @Override
    public Optional<ImmutableExpression> getOptionalFilterCondition() {
        return optionalFilterCondition;
    }

    protected String getOptionalFilterString() {
        if (optionalFilterCondition.isPresent()) {
            return " " + optionalFilterCondition.get().toString();
        }

        return "";
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        if (optionalFilterCondition.isPresent()) {
            return optionalFilterCondition.get().getVariables();
        }
        else {
            return ImmutableSet.of();
        }
    }

    protected ExpressionEvaluator.EvaluationResult transformBooleanExpression(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            ImmutableExpression booleanExpression) {

        ImmutableExpression substitutedExpression = substitution.applyToBooleanExpression(booleanExpression);

        return createExpressionEvaluator().evaluateExpression(substitutedExpression);
    }

    protected ExpressionEvaluator createExpressionEvaluator() {
        return defaultExpressionEvaluator.clone();
    }

    protected boolean isFilteringNullValue(Variable variable) {
        return getOptionalFilterCondition()
                .filter(e -> nullabilityEvaluator.isFilteringNullValue(e, variable))
                .isPresent();
    }

    protected VariableNullability updateWithFilter(ImmutableExpression filter,
                                                   ImmutableSet<ImmutableSet<Variable>> nullableGroups) {
        ImmutableSet<ImmutableSet<Variable>> newNullableGroups = nullableGroups.stream()
                .filter(g -> !nullabilityEvaluator.isFilteringNullValues(filter, g))
                .collect(ImmutableCollectors.toSet());

        return new VariableNullabilityImpl(newNullableGroups);
    }

    protected TermNullabilityEvaluator getNullabilityEvaluator() {
        return nullabilityEvaluator;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    protected ExpressionAndSubstitution simplifyCondition(Optional<ImmutableExpression> nonOptimizedExpression,
                                                          ImmutableSet<Variable> nonLiftableVariables)
            throws UnsatisfiableConditionException {

        Optional<ExpressionEvaluator.EvaluationResult> optionalEvaluationResults =
                nonOptimizedExpression
                        .map(e -> createExpressionEvaluator().evaluateExpression(e));

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

        return new ExpressionAndSubstitution(newExpression,
                normalizedUnifier.reduceDomainToIntersectionWith(
                        Sets.difference(normalizedUnifier.getDomain(), nonLiftableVariables).immutableCopy()));
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

    protected Optional<ImmutableExpression> computeDownConstraint(Optional<ImmutableExpression> optionalConstraint,
                                                                  ExpressionAndSubstitution conditionSimplificationResults)
            throws UnsatisfiableConditionException {
        if (optionalConstraint.isPresent()) {
            ImmutableExpression substitutedConstraint = conditionSimplificationResults.substitution
                    .applyToBooleanExpression(optionalConstraint.get());

            ImmutableExpression combinedExpression = conditionSimplificationResults.optionalExpression
                    .flatMap(e -> immutabilityTools.foldBooleanExpressions(
                            Stream.concat(
                                    e.flattenAND().stream(),
                                    substitutedConstraint.flattenAND().stream())))
                    .orElse(substitutedConstraint);

            ExpressionEvaluator.EvaluationResult evaluationResults = createExpressionEvaluator()
                    .evaluateExpression(combinedExpression);

            if (evaluationResults.isEffectiveFalse())
                throw new UnsatisfiableConditionException();

            return evaluationResults.getOptionalExpression();
        }
        else
            return conditionSimplificationResults.optionalExpression;
    }

    protected void checkExpression(ImmutableExpression expression, ImmutableList<IQTree> children)
            throws InvalidIntermediateQueryException {

        ImmutableSet<Variable> childrenVariables = children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> unboundVariables = expression.getVariableStream()
                .filter(v -> !childrenVariables.contains(v))
                .collect(ImmutableCollectors.toSet());
        if (!unboundVariables.isEmpty()) {
            throw new InvalidIntermediateQueryException("Expression " + expression + " of "
                    + expression + " uses unbound variables (" + unboundVariables +  ").\n" + this);
        }
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

    protected static class UnsatisfiableConditionException extends Exception {
    }


}
