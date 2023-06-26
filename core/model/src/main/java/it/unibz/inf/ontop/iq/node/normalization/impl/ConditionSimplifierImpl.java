package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class ConditionSimplifierImpl implements ConditionSimplifier {

    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    private ConditionSimplifierImpl(SubstitutionFactory substitutionFactory,
                                    TermFactory termFactory) {
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }


    @Override
    public ExpressionAndSubstitution simplifyCondition(ImmutableExpression expression, ImmutableList<IQTree> children,
                                                       VariableNullability variableNullability)
            throws UnsatisfiableConditionException {
        return simplifyCondition(Optional.of(expression), ImmutableSet.of(), children, variableNullability);
    }

    @Override
    public ExpressionAndSubstitution simplifyCondition(Optional<ImmutableExpression> nonOptimizedExpression,
                                                       ImmutableSet<Variable> nonLiftableVariables,
                                                       ImmutableList<IQTree> children,
                                                       VariableNullability variableNullability)
            throws UnsatisfiableConditionException {

        if (nonOptimizedExpression.isPresent()) {

            Optional<ImmutableExpression> optionalExpression = evaluateCondition(nonOptimizedExpression.get(),
                    variableNullability);
            if (optionalExpression.isPresent())
                // May throw an exception if unification is rejected
                return convertIntoExpressionAndSubstitution(optionalExpression.get(), nonLiftableVariables, children, variableNullability);
            else
                return new ExpressionAndSubstitutionImpl(Optional.empty(), substitutionFactory.getSubstitution());
        }
        else
            return new ExpressionAndSubstitutionImpl(Optional.empty(), substitutionFactory.getSubstitution());
    }

    /**
     * Empty means true
     */
    private Optional<ImmutableExpression> evaluateCondition(ImmutableExpression expression,
                                                            VariableNullability variableNullability) throws UnsatisfiableConditionException {
        ImmutableExpression.Evaluation results = expression.evaluate2VL(variableNullability);

        if (results.isEffectiveFalse())
            throw new UnsatisfiableConditionException();

        return results.getExpression();
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
            throws UnsatisfiableConditionException {

        ImmutableSet<ImmutableExpression> expressions = expression.flattenAND()
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<ImmutableExpression> functionFreeEqualities = expressions.stream()
                .filter(e -> e.getFunctionSymbol() instanceof DBStrictEqFunctionSymbol)
                // TODO: consider the fact that equalities might be n-ary
                .filter(e -> e.getTerms().stream().allMatch(t -> t instanceof NonFunctionalTerm))
                .collect(ImmutableCollectors.toSet());

        Substitution<NonFunctionalTerm> normalizedUnifier = substitutionFactory.onNonFunctionalTerms().unifierBuilder()
                .unify(functionFreeEqualities.stream(), eq -> (NonFunctionalTerm)eq.getTerm(0), eq -> (NonFunctionalTerm)eq.getTerm(1))
                .build()
                // TODO: merge priorityRenaming with the orientate() method
                .map(u -> substitutionFactory.onNonFunctionalTerms().compose(substitutionFactory.getPrioritizingRenaming(u, nonLiftableVariables), u))
                .orElseThrow(UnsatisfiableConditionException::new);

        ImmutableSet<Variable> rejectedByChildrenVariablesEqToConstant = normalizedUnifier.getDomain().stream()
                .filter(v -> children.stream()
                        .filter(c -> c.getVariables().contains(v))
                        .allMatch(c -> c.getRootNode().wouldKeepDescendingGroundTermInFilterAbove(v, true)))
                .collect(ImmutableCollectors.toSet());

        Sets.SetView<Variable> variablesToRemainInEqualities = Sets.union(nonLiftableVariables, rejectedByChildrenVariablesEqToConstant);

        Optional<ImmutableExpression> partiallySimplifiedExpression = termFactory.getConjunction(
                Stream.concat(
                        // Expressions that are not function-free equalities
                        expressions.stream()
                                .filter(e -> !functionFreeEqualities.contains(e))
                                .map(normalizedUnifier::apply),

                        // Equalities that must remain
                        normalizedUnifier.builder()
                                .restrictDomainTo(variablesToRemainInEqualities)
                                .toStream(termFactory::getStrictEquality)
                                .sorted(Comparator.comparing(eq -> (Variable) eq.getTerm(0)))));

        Optional<Substitution<GroundFunctionalTerm>> groundFunctionalSubstitution = partiallySimplifiedExpression
                .flatMap(e -> extractGroundFunctionalSubstitution(expression, children));

        Optional<ImmutableExpression> newExpression = groundFunctionalSubstitution.isPresent()
            ? evaluateCondition(
                groundFunctionalSubstitution.get().apply(partiallySimplifiedExpression.get()),
                    variableNullability)
            : partiallySimplifiedExpression;

        Substitution<VariableOrGroundTerm> ascendingSubstitution = substitutionFactory.union(
                        normalizedUnifier.removeFromDomain(variablesToRemainInEqualities),
                        groundFunctionalSubstitution.orElseGet(substitutionFactory::getSubstitution));

        return new ExpressionAndSubstitutionImpl(newExpression, ascendingSubstitution);
    }

    @Override
    public Optional<ImmutableExpression> computeDownConstraint(Optional<ImmutableExpression> optionalConstraint,
                                                               ExpressionAndSubstitution conditionSimplificationResults,
                                                               VariableNullability childVariableNullability) throws UnsatisfiableConditionException {
        if (optionalConstraint.isPresent()) {
            ImmutableExpression substitutedConstraint =
                    conditionSimplificationResults.getSubstitution().apply(optionalConstraint.get());

            ImmutableExpression combinedExpression = conditionSimplificationResults.getOptionalExpression()
                    .flatMap(e -> termFactory.getConjunction(Stream.of(e, substitutedConstraint)))
                    .orElse(substitutedConstraint);

            ImmutableExpression.Evaluation evaluationResults = combinedExpression.evaluate2VL(childVariableNullability);

            if (evaluationResults.isEffectiveFalse())
                throw new UnsatisfiableConditionException();

            return evaluationResults.getExpression();
        }
        else
            return conditionSimplificationResults.getOptionalExpression();
    }


    /**
     * We can extract at most one equality ground-functional-term -> variable per variable.
     * Treated differently from non-functional terms because functional terms are not robust to unification.
     * Does not include in the substitution ground terms that are "rejected" by all the children using the variable
     */
    private Optional<Substitution<GroundFunctionalTerm>> extractGroundFunctionalSubstitution(
            ImmutableExpression expression, ImmutableList<IQTree> children) {

        ImmutableMultimap<Variable, GroundFunctionalTerm> binaryEqualitiesSubset = expression.flattenAND()
                .filter(e -> e.getFunctionSymbol() instanceof DBStrictEqFunctionSymbol)
                .map(ImmutableFunctionalTerm::getTerms)
                .filter(args -> args.stream().anyMatch(t -> t instanceof Variable)
                        && args.stream().anyMatch(t -> t instanceof GroundFunctionalTerm)
                        && args.stream().allMatch(t -> t instanceof Variable || t instanceof GroundFunctionalTerm))
                .flatMap(args -> args.stream()
                        .filter(t -> t instanceof Variable)
                        .map(t -> (Variable)t)
                        .flatMap(v -> args.stream()
                                .filter(t -> t instanceof GroundFunctionalTerm)
                                .map(t -> (GroundFunctionalTerm)t)
                                .map(g -> Maps.immutableEntry(v, g))))
                .collect(ImmutableCollectors.toMultimap());

        return Optional.of(binaryEqualitiesSubset)
                .map(m -> m.asMap().entrySet().stream()
                        // Filter out ground terms that would be "rejected" by all the children using the variable
                        .filter(e -> children.stream()
                                .filter(c -> c.getVariables().contains(e.getKey()))
                                .anyMatch(c -> !c.getRootNode().wouldKeepDescendingGroundTermInFilterAbove(e.getKey(), false)))
                        .collect(substitutionFactory.toSubstitution(
                                Map.Entry::getKey,
                                // Picks one of the ground functional terms
                                e -> e.getValue().iterator().next())))
                .filter(s -> !s.isEmpty());
    }
}
