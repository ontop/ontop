package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

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
    public <R> R liftRegularChildBinding(ConstructionNode selectedChildConstructionNode,
                                         int selectedChildPosition,
                                         IQTree selectedGrandChild,
                                         ImmutableList<IQTree> children,
                                         ImmutableSet<Variable> nonLiftableVariables,
                                         Optional<ImmutableExpression> initialJoiningCondition,
                                         VariableGenerator variableGenerator,
                                         VariableNullability variableNullability,
                                         BindingLiftConverter<R> bindingLiftConverter) throws UnsatisfiableConditionException {

        ImmutableSubstitution<ImmutableTerm> selectedChildSubstitution = selectedChildConstructionNode.getSubstitution();

        ImmutableSubstitution<VariableOrGroundTerm> downPropagableFragment = selectedChildSubstitution
                .getFragment(VariableOrGroundTerm.class);

        ImmutableSubstitution<ImmutableFunctionalTerm> nonDownPropagableFragment = selectedChildSubstitution.getFragment(ImmutableFunctionalTerm.class);

        ImmutableSet<Variable> otherChildrenVariables = IntStream.range(0, children.size())
                .filter(i -> i != selectedChildPosition)
                .mapToObj(children::get)
                .flatMap(iq -> iq.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        InjectiveVar2VarSubstitution freshRenaming = substitutionFactory.getInjectiveVar2VarSubstitution(
                nonDownPropagableFragment.getImmutableMap().keySet().stream()
                        .filter(otherChildrenVariables::contains),
                variableGenerator::generateNewVariableFromVar);

        ConditionSimplifier.ExpressionAndSubstitution expressionResults = conditionSimplifier.simplifyCondition(
                computeNonOptimizedCondition(initialJoiningCondition, selectedChildSubstitution, freshRenaming),
                nonLiftableVariables, variableNullability);
        Optional<ImmutableExpression> newCondition = expressionResults.getOptionalExpression();

        // NB: this substitution is said to be "naive" as further restrictions may be applied
        // to the effective ascending substitution (e.g., for the LJ, in the case of the renaming of right-specific vars)
        ImmutableSubstitution<ImmutableTerm> naiveAscendingSubstitution = expressionResults.getSubstitution().composeWith(
                selectedChildSubstitution);
        ImmutableSubstitution<VariableOrGroundTerm> descendingSubstitution =
                expressionResults.getSubstitution().composeWith2(freshRenaming)
                        .composeWith2(downPropagableFragment);

        return bindingLiftConverter.convert(children, selectedGrandChild, selectedChildPosition, newCondition,
                naiveAscendingSubstitution, descendingSubstitution);
    }

    private Optional<ImmutableExpression> computeNonOptimizedCondition(Optional<ImmutableExpression> initialJoiningCondition,
                                                                       ImmutableSubstitution<ImmutableTerm> substitution,
                                                                       InjectiveVar2VarSubstitution freshRenaming) {

        Stream<ImmutableExpression> expressions2 = freshRenaming.getImmutableMap().entrySet().stream()
                        .map(r -> termFactory.getStrictEquality(substitution.applyToVariable(r.getKey()), r.getValue()));

        return termFactory.getConjunction(
                initialJoiningCondition.map(substitution::applyToBooleanExpression), expressions2);
    }

    @FunctionalInterface
    public interface BindingLiftConverter<R> {

        R convert(ImmutableList<IQTree> liftedChildren, IQTree selectedGrandChild, int selectedChildPosition,
                  Optional<ImmutableExpression> newCondition,
                  ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
                  ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution);
    }
}
