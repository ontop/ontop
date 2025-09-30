package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
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
import java.util.stream.Stream;

@Singleton
public class ConditionSimplifierImpl implements ConditionSimplifier {

    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private ConditionSimplifierImpl(SubstitutionFactory substitutionFactory,
                                    TermFactory termFactory, IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory) {
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
        this.iqFactory = iqFactory;
    }


    @Override
    public ExpressionAndSubstitution simplifyCondition(Optional<ImmutableExpression> nonOptimizedExpression,
                                                       ImmutableSet<Variable> nonLiftableVariables,
                                                       ImmutableList<IQTree> children,
                                                       VariableNullability variableNullability)
            throws DownPropagation.InconsistentDownPropagationException {

        if (nonOptimizedExpression.isPresent()) {
            Optional<ImmutableExpression> optionalExpression = evaluateCondition(nonOptimizedExpression.get(), variableNullability);
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
                .build()
                // TODO: merge priorityRenaming with the orientate() method
                .map(u -> substitutionFactory.onNonFunctionalTerms().compose(substitutionFactory.getPrioritizingRenaming(u, nonLiftableVariables), u))
                .orElseThrow(DownPropagation.InconsistentDownPropagationException::new);

        ImmutableSet<Variable> rejectedByChildrenVariablesEqToConstant = normalizedUnifier.getDomain().stream()
                .filter(v -> children.stream()
                        .filter(c -> c.getVariables().contains(v))
                        .allMatch(c -> c.getRootNode().wouldKeepDescendingGroundTermInFilterAbove(v, true)))
                .collect(ImmutableCollectors.toSet());

        Set<Variable> variablesToRemainInEqualities = Sets.union(nonLiftableVariables, rejectedByChildrenVariablesEqToConstant);

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
    public ExpressionAndSubstitutionAndChildren simplifyAndPropagate(DownPropagation downPropagation, Optional<ImmutableExpression> expression, ImmutableList<IQTree> children,
                                                                     VariableNullability variableNullability) throws DownPropagation.InconsistentDownPropagationException {
        // TODO: also consider the constraint for simplifying the condition
        var simplification = simplifyCondition(
                downPropagation.withSubstitution(expression,
                        (ds, e) -> e.map(ds::apply)),
                ImmutableSet.of(),
                children,
                variableNullability);

        var downSubstitution =
                downPropagation.withSubstitution(simplification.getSubstitution(),
                        (ds, s) -> substitutionFactory.onVariableOrGroundTerms().compose(ds, s));

        var newConstraint = iqTreeTools.updateDownPropagationConstraint(downPropagation, simplification.getSubstitution(), simplification.getOptionalExpression(), () -> variableNullability);

        var extendedDownConstraint = iqTreeTools.createDownPropagation(downSubstitution, newConstraint, downPropagation.getVariables(), downPropagation.getVariableGenerator());

        return new ExpressionAndSubstitutionAndChildrenImpl(
                iqTreeTools.createOptionalConstructionNode(downPropagation::computeProjectedVariables, simplification.getSubstitution()),
                simplification.getOptionalExpression(),
                NaryIQTreeTools.transformChildren(children, extendedDownConstraint::propagateToChild));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class ExpressionAndSubstitutionAndChildrenImpl implements ExpressionAndSubstitutionAndChildren {
        private final Optional<ConstructionNode> optionalConstructionNode;
        private final Optional<ImmutableExpression> optionalExpression;
        private final ImmutableList<IQTree> children;

        private ExpressionAndSubstitutionAndChildrenImpl(Optional<ConstructionNode> optionalConstructionNode, Optional<ImmutableExpression> optionalExpression, ImmutableList<IQTree> children) {
            this.optionalConstructionNode = optionalConstructionNode;
            this.optionalExpression = optionalExpression;
            this.children = children;
        }

        @Override
        public Optional<ConstructionNode> getConstructionNode() {
            return optionalConstructionNode;
        }

        @Override
        public Optional<ImmutableExpression> getOptionalExpression() {
            return optionalExpression;
        }

        @Override
        public ImmutableList<IQTree> getChildren() {
            return children;
        }
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
    private Optional<Substitution<GroundFunctionalTerm>> extractGroundFunctionalSubstitution(
            ImmutableExpression expression, ImmutableList<IQTree> children) {

        ImmutableMultimap<Variable, GroundFunctionalTerm> binaryEqualitiesSubset = expression.flattenAND()
                .filter(e -> e.getFunctionSymbol() instanceof DBStrictEqFunctionSymbol)
                .map(ImmutableFunctionalTerm::getTerms)
                .filter(args ->
                        args.stream().anyMatch(t -> t instanceof Variable)
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
