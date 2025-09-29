package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Singleton
public class JoinLikeChildBindingLifter {

    private final ConditionSimplifier conditionSimplifier;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private JoinLikeChildBindingLifter(ConditionSimplifier conditionSimplifier, TermFactory termFactory,
                                       SubstitutionFactory substitutionFactory) {
        this.conditionSimplifier = conditionSimplifier;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
    }

    /**
     * For children of a commutative join or for the left child of a LJ
     */
    public BindingLift liftRegularChildBinding(ConstructionNode selectedChildConstructionNode,
                                         int selectedChildPosition,
                                         ImmutableList<IQTree> children,
                                         ImmutableSet<Variable> nonLiftableVariables,
                                         Optional<ImmutableExpression> initialJoiningCondition,
                                         VariableGenerator variableGenerator,
                                         VariableNullability variableNullability) throws DownPropagation.InconsistentDownPropagationException {

        Substitution<ImmutableTerm> substitution = selectedChildConstructionNode.getSubstitution();

        Substitution<VariableOrGroundTerm> downPropagableFragment = substitution.restrictRangeTo(VariableOrGroundTerm.class);

        Substitution<ImmutableFunctionalTerm> nonDownPropagableFragment = substitution.restrictRangeTo(ImmutableFunctionalTerm.class);

        ImmutableSet<Variable> otherChildrenVariables = IntStream.range(0, children.size())
                .filter(i -> i != selectedChildPosition)
                .mapToObj(children::get)
                .map(IQTree::getVariables)
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toSet());

        InjectiveSubstitution<Variable> freshRenaming = Sets.intersection(nonDownPropagableFragment.getDomain(), otherChildrenVariables).stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

        Stream<ImmutableExpression> equalities = freshRenaming.builder()
                .toStream((v, t) -> termFactory.getStrictEquality(substitution.apply(v), t));

        ConditionSimplifier.ExpressionAndSubstitution expressionResults = conditionSimplifier.simplifyCondition(
                termFactory.getConjunction(initialJoiningCondition.map(substitution::apply), equalities),
                nonLiftableVariables, children, variableNullability);

        Optional<ImmutableExpression> newCondition = expressionResults.getOptionalExpression();

        // NB: this substitution is said to be "naive" as further restrictions may be applied
        // to the effective ascending substitution (e.g., for the LJ, in the case of the renaming of right-specific vars)
        Substitution<ImmutableTerm> naiveAscendingSubstitution =
                expressionResults.getSubstitution().compose(substitution);

        Substitution<VariableOrGroundTerm> descendingSubstitution =
                substitutionFactory.onVariableOrGroundTerms().compose(
                        expressionResults.getSubstitution(),
                        substitutionFactory.union(freshRenaming, downPropagableFragment.removeFromDomain(freshRenaming.getDomain())));

        return new BindingLift(newCondition, naiveAscendingSubstitution, descendingSubstitution);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class BindingLift {
        private final Optional<ImmutableExpression> newCondition;
        private final Substitution<ImmutableTerm> ascendingSubstitution;
        private final Substitution<? extends VariableOrGroundTerm> descendingSubstitution;

        private BindingLift(Optional<ImmutableExpression> newCondition, Substitution<ImmutableTerm> ascendingSubstitution, Substitution<? extends VariableOrGroundTerm> descendingSubstitution) {
            this.newCondition = newCondition;
            this.ascendingSubstitution = ascendingSubstitution;
            this.descendingSubstitution = descendingSubstitution;
        }

        public Optional<ImmutableExpression> getCondition() {
            return newCondition;
        }

        public Substitution<ImmutableTerm> getAscendingSubstitution() {
            return ascendingSubstitution;
        }

        public Substitution<? extends VariableOrGroundTerm> getDescendingSubstitution() {
            return descendingSubstitution;
        }
    }
}
