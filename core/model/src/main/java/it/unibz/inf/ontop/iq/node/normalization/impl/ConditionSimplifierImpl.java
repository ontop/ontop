package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Singleton
public class ConditionSimplifierImpl implements ConditionSimplifier {

    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private ConditionSimplifierImpl(SubstitutionFactory substitutionFactory,
                                    TermFactory termFactory, IQTreeTools iqTreeTools) {
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
    }


    @Override
    public ExpressionAndSubstitution simplifyCondition(Optional<ImmutableExpression> nonOptimizedExpression,
                                                       ImmutableSet<Variable> nonLiftableVariables,
                                                       ImmutableList<IQTree> children,
                                                       VariableNullability variableNullability)
            throws DownPropagation.InconsistentDownPropagationException {

        return simplifyCondition(
                nonOptimizedExpression,
                e -> variableNullability,
                e -> convertIntoExpressionAndSubstitution(e, nonLiftableVariables, children, variableNullability));
    }

    @Override
    public ExpressionAndSubstitution simplifyConditionForLeftJoin(Optional<ImmutableExpression> nonOptimizedExpression, Function<ImmutableExpression, VariableNullability> variableNullability, ImmutableSet<Variable> leftVariables, ImmutableSet<Variable> rightVariables) throws DownPropagation.InconsistentDownPropagationException {
        return simplifyCondition(
                nonOptimizedExpression,
                variableNullability,
                e -> convertIntoExpressionAndSubstitutionForLeftJoin(e, leftVariables, rightVariables));
    }

    private interface Extractor {
        ExpressionAndSubstitution extract(ImmutableExpression expression) throws DownPropagation.InconsistentDownPropagationException;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private ExpressionAndSubstitution simplifyCondition(Optional<ImmutableExpression> optionalExpression, Function<ImmutableExpression, VariableNullability> variableNullabilitySupplier, Extractor extractor) throws DownPropagation.InconsistentDownPropagationException {
        if (optionalExpression.isPresent()) {
            var expression = optionalExpression.get();
            var variableNullability = variableNullabilitySupplier.apply(expression);
            var optionalSimplifiedExpression = evaluateCondition(expression, variableNullability);
            if (optionalSimplifiedExpression.isPresent())
                // May throw an exception if unification is rejected
                return extractor.extract(optionalSimplifiedExpression.get());
            else
                return new ExpressionAndSubstitutionImpl(Optional.empty(), substitutionFactory.getSubstitution());
        }
        else
            return new ExpressionAndSubstitutionImpl(Optional.empty(), substitutionFactory.getSubstitution());
    }


    /**
     * TODO: explain
     *
     * Functional terms remain in the expression (never going into the substitution)
     *
     */
    private ExpressionAndSubstitution convertIntoExpressionAndSubstitution(ImmutableExpression expression,
                                                                           ImmutableSet<Variable> nonLiftableVariables,
                                                                           ImmutableList<IQTree> children, VariableNullability variableNullability)
            throws DownPropagation.InconsistentDownPropagationException {

        ImmutableSet<ImmutableExpression> expressions = expression.flattenAND()
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<ImmutableExpression> functionFreeEqualities = expressions.stream()
                .filter(e -> e.getFunctionSymbol() instanceof DBStrictEqFunctionSymbol)
                // TODO: consider the fact that equalities might be n-ary
                .filter(e -> e.getTerms().stream().allMatch(t -> t instanceof NonFunctionalTerm))
                .collect(ImmutableCollectors.toSet());

        Substitution<NonFunctionalTerm> normalizedUnifier = substitutionFactory.onNonFunctionalTerms().unifierBuilder()
                .unify(functionFreeEqualities.stream(), eq -> (NonFunctionalTerm)eq.getTerm(0), eq -> (NonFunctionalTerm)eq.getTerm(1))
                .buildNormalized(nonLiftableVariables)
                .orElseThrow(DownPropagation.InconsistentDownPropagationException::new);

        ImmutableSet<Variable> rejectedByChildrenVariablesEqToConstant = normalizedUnifier.getDomain().stream()
                .filter(v -> children.stream()
                        .filter(c -> c.getVariables().contains(v))
                        .allMatch(c -> c.getRootNode().wouldKeepDescendingGroundTermInFilterAbove(v, true)))
                .collect(ImmutableCollectors.toSet());

        Set<Variable> variablesToRemainInEqualities = Sets.union(nonLiftableVariables, rejectedByChildrenVariablesEqToConstant);

        Optional<ImmutableExpression> partiallySimplifiedExpression = termFactory.getConjunction(
                Stream.concat(
                        expressions.stream()
                                .filter(e -> !functionFreeEqualities.contains(e))
                                .map(normalizedUnifier::apply),

                        // Equalities that must remain
                        normalizedUnifier.builder()
                                .restrictDomainTo(variablesToRemainInEqualities)
                                .toStream(termFactory::getStrictEquality)
                                .sorted(Comparator.comparing(eq -> (Variable) eq.getTerm(0)))));

        Substitution<GroundFunctionalTerm> groundFunctionalSubstitution = partiallySimplifiedExpression
                .map(e -> extractGroundFunctionalSubstitution(expression, children))
                .orElseGet(substitutionFactory::getSubstitution);

        Optional<ImmutableExpression> newExpression = !groundFunctionalSubstitution.isEmpty()
            ? evaluateCondition(
                groundFunctionalSubstitution.apply(partiallySimplifiedExpression.get()),
                    variableNullability)
            : partiallySimplifiedExpression;

        Substitution<VariableOrGroundTerm> ascendingSubstitution = substitutionFactory.union(
                        normalizedUnifier.removeFromDomain(variablesToRemainInEqualities),
                        groundFunctionalSubstitution);

        return new ExpressionAndSubstitutionImpl(newExpression, ascendingSubstitution);
    }

    @Override
    public DownPropagation getCombinedDownPropagation(DownPropagation dp, ExpressionAndSubstitution simplification, VariableNullability variableNullability) throws DownPropagation.InconsistentDownPropagationException {
        var newConstraint = dp.getConstraint().isPresent()
                ? evaluateCondition(
                iqTreeTools.getConjunction(simplification.getOptionalExpression(), simplification.getSubstitution().apply(dp.getConstraint().get())),
                dp.extendVariableNullability(variableNullability))
                : simplification.getOptionalExpression();

        var downSubstitution = substitutionFactory.onVariableOrGroundTerms().compose(
                simplification.getSubstitution(),
                dp.getDescendingSubstitution());

        return iqTreeTools.createDownPropagation(downSubstitution, newConstraint, dp.getVariables(), dp.getVariableGenerator());
    }


    /**
     * Empty means true
     */
    public static Optional<ImmutableExpression> evaluateCondition(ImmutableExpression expression,
                                                                  VariableNullability variableNullability) throws DownPropagation.InconsistentDownPropagationException {
        ImmutableExpression.Evaluation results = expression.evaluate2VL(variableNullability);

        if (results.isEffectiveFalse())
            throw new DownPropagation.InconsistentDownPropagationException();

        return results.getExpression();
    }



    /**
     * We can extract at most one equality ground-functional-term -> variable per variable.
     * Treated differently from non-functional terms because functional terms are not robust to unification.
     * Does not include in the substitution ground terms that are "rejected" by all the children using the variable
     */
    private Substitution<GroundFunctionalTerm> extractGroundFunctionalSubstitution(
            ImmutableExpression expression, ImmutableList<IQTree> children) {

        ImmutableMultimap<Variable, GroundFunctionalTerm> binaryEqualitiesSubset = expression.flattenAND()
                .filter(e -> e.getFunctionSymbol() instanceof DBStrictEqFunctionSymbol)
                .map(ImmutableFunctionalTerm::getTerms)
                .filter(args ->
                        args.stream().allMatch(t -> t instanceof Variable || t instanceof GroundFunctionalTerm))
                .flatMap(args -> args.stream()
                        .filter(t -> t instanceof Variable)
                        .flatMap(v -> args.stream()
                                .filter(t -> t instanceof GroundFunctionalTerm)
                                .map(t -> Maps.immutableEntry((Variable)v, (GroundFunctionalTerm)t))))
                .collect(ImmutableCollectors.toMultimap());

        return binaryEqualitiesSubset.asMap().entrySet().stream()
                // Filter out ground terms that would be "rejected" by all the children using the variable
                .filter(e -> children.stream()
                        .filter(c -> c.getVariables().contains(e.getKey()))
                        .anyMatch(c -> !c.getRootNode().wouldKeepDescendingGroundTermInFilterAbove(e.getKey(), false)))
                .collect(substitutionFactory.toSubstitution(
                        Map.Entry::getKey,
                        // Picks one of the ground functional terms
                        e -> e.getValue().iterator().next()));
    }


    /**
     * TODO: explain
     *
     */
    private ExpressionAndSubstitution convertIntoExpressionAndSubstitutionForLeftJoin(ImmutableExpression expression,
                                                                           ImmutableSet<Variable> leftVariables,
                                                                           ImmutableSet<Variable> rightVariables) {

        Set<Variable> rightSpecificVariables = Sets.difference(rightVariables, leftVariables);

        ImmutableSet<ImmutableExpression> expressions = expression.flattenAND()
                .collect(ImmutableCollectors.toSet());
        ImmutableSet<ImmutableExpression> downSubstitutionExpressions = expressions.stream()
                .filter(e -> e.getFunctionSymbol() instanceof DBStrictEqFunctionSymbol)
                // TODO: refactor it for dealing with n-ary EQs
                .filter(e -> e.getTerms().stream().allMatch(t -> t instanceof NonFunctionalTerm)
                        && e.getTerms().stream().anyMatch(rightVariables::contains))
                .collect(ImmutableCollectors.toSet());

        Substitution<VariableOrGroundTerm> downSubstitution = downSubstitutionExpressions.stream()
                .map(ImmutableFunctionalTerm::getTerms)
                .map(args -> (args.get(0) instanceof Variable) ? args : args.reverse())
                // Rename right-specific variables if possible
                .map(args -> ((args.get(0) instanceof Variable) && rightSpecificVariables.contains(args.get(1)))
                        ? args.reverse() : args)
                .collect(substitutionFactory.toSubstitution(
                        args -> (Variable) args.get(0),
                        args -> (VariableOrGroundTerm) args.get(1)));

        Optional<ImmutableExpression> newExpression = Optional.of(expressions.stream()
                        .filter(e -> !downSubstitutionExpressions.contains(e)
                                || e.getTerms().stream().anyMatch(rightSpecificVariables::contains))
                        .collect(ImmutableCollectors.toList()))
                .filter(l -> !l.isEmpty())
                .map(termFactory::getConjunction)
                .map(downSubstitution::apply);

        return new ExpressionAndSubstitutionImpl(newExpression, downSubstitution);
    }

    private static class ExpressionAndSubstitutionImpl implements ExpressionAndSubstitution {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final Optional<ImmutableExpression> optionalExpression;
        private final Substitution<VariableOrGroundTerm> substitution;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public ExpressionAndSubstitutionImpl(Optional<ImmutableExpression> optionalExpression,
                                             Substitution<VariableOrGroundTerm> substitution) {
            this.optionalExpression = optionalExpression;
            this.substitution = substitution;
        }

        @Override
        public Substitution<VariableOrGroundTerm> getSubstitution() {
            return substitution;
        }

        @Override
        public Optional<ImmutableExpression> getOptionalExpression() {
            return optionalExpression;
        }
    }
}
