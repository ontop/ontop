package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;

@Singleton
public class JoinLikeChildBindingLifter {

    private final ConditionSimplifier conditionSimplifier;
    private final TermFactory termFactory;
    private final ImmutabilityTools immutabilityTools;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private JoinLikeChildBindingLifter(ConditionSimplifier conditionSimplifier, TermFactory termFactory,
                                       ImmutabilityTools immutabilityTools, SubstitutionFactory substitutionFactory) {
        this.conditionSimplifier = conditionSimplifier;
        this.termFactory = termFactory;
        this.immutabilityTools = immutabilityTools;
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
                                            BindingLiftConverter<R> bindingLiftConverter) throws UnsatisfiableConditionException {

        ImmutableSubstitution<ImmutableTerm> selectedChildSubstitution = selectedChildConstructionNode.getSubstitution();

        ImmutableSubstitution<NonFunctionalTerm> downPropagableFragment = selectedChildSubstitution
                .getNonFunctionalTermFragment();

        ImmutableSubstitution<ImmutableFunctionalTerm> nonDownPropagableFragment = selectedChildSubstitution.getFunctionalTermFragment();


        ImmutableSet<Variable> otherChildrenVariables = IntStream.range(0, children.size())
                .filter(i -> i != selectedChildPosition)
                .boxed()
                .map(children::get)
                .flatMap(iq -> iq.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        InjectiveVar2VarSubstitution freshRenaming = computeOtherChildrenRenaming(nonDownPropagableFragment,
                otherChildrenVariables, variableGenerator);

        ConditionSimplifier.ExpressionAndSubstitution expressionResults = conditionSimplifier.simplifyCondition(
                computeNonOptimizedCondition(initialJoiningCondition, selectedChildSubstitution, freshRenaming),
                nonLiftableVariables);
        Optional<ImmutableExpression> newCondition = expressionResults.getOptionalExpression();

        ImmutableSubstitution<ImmutableTerm> ascendingSubstitution = expressionResults.getSubstitution().composeWith(
                selectedChildSubstitution);
        ImmutableSubstitution<NonFunctionalTerm> descendingSubstitution =
                expressionResults.getSubstitution().composeWith2(freshRenaming)
                        .composeWith2(downPropagableFragment);

        return bindingLiftConverter.convert(children, selectedGrandChild, selectedChildPosition, newCondition,
                ascendingSubstitution, descendingSubstitution);
    }

    private Optional<ImmutableExpression> computeNonOptimizedCondition(Optional<ImmutableExpression> initialJoiningCondition,
                                                                       ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                                                       InjectiveVar2VarSubstitution freshRenaming) {
        Stream<ImmutableExpression> expressions = Stream.concat(
                initialJoiningCondition
                        .map(substitution::applyToBooleanExpression)
                        .map(ImmutableExpression::flattenAND)
                        .orElseGet(ImmutableSet::of)
                        .stream(),
                freshRenaming.getImmutableMap().entrySet().stream()
                        .map(r -> termFactory.getImmutableExpression(EQ,
                                substitution.applyToVariable(r.getKey()),
                                r.getValue())));

        return immutabilityTools.foldBooleanExpressions(expressions);
    }

    private InjectiveVar2VarSubstitution computeOtherChildrenRenaming(ImmutableSubstitution<ImmutableFunctionalTerm> nonDownPropagatedFragment,
                                                                      ImmutableSet<Variable> otherChildrenVariables,
                                                                      VariableGenerator variableGenerator) {
        ImmutableMap<Variable, Variable> substitutionMap = nonDownPropagatedFragment.getImmutableMap().keySet().stream()
                .filter(otherChildrenVariables::contains)
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        variableGenerator::generateNewVariableFromVar));
        return substitutionFactory.getInjectiveVar2VarSubstitution(substitutionMap);
    }

    @FunctionalInterface
    public interface BindingLiftConverter<R> {

        R convert(ImmutableList<IQTree> liftedChildren, IQTree selectedGrandChild, int selectedChildPosition,
                  Optional<ImmutableExpression> newCondition,
                  ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
                  ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution);
    }
}
