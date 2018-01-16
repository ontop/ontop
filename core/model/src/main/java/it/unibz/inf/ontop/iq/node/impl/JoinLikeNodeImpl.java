package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class JoinLikeNodeImpl extends JoinOrFilterNodeImpl implements JoinLikeNode {

    protected JoinLikeNodeImpl(Optional<ImmutableExpression> optionalJoinCondition,
                               TermNullabilityEvaluator nullabilityEvaluator,
                               TermFactory termFactory, IntermediateQueryFactory iqFactory,
                               TypeFactory typeFactory, DatalogTools datalogTools,
                               ExpressionEvaluator defaultExpressionEvaluator,
                               ImmutabilityTools immutabilityTools, SubstitutionFactory substitutionFactory,
                               ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools) {
        super(optionalJoinCondition, nullabilityEvaluator, termFactory, iqFactory, typeFactory, datalogTools,
                immutabilityTools, substitutionFactory, unificationTools, substitutionTools, defaultExpressionEvaluator);
    }

    /**
     * TODO: explain
     */
    protected Optional<ExpressionEvaluator.EvaluationResult> computeAndEvaluateNewCondition(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            Optional<ImmutableExpression> optionalNewEqualities) {

        Optional<ImmutableExpression> updatedExistingCondition = getOptionalFilterCondition()
                .map(substitution::applyToBooleanExpression);

        Optional<ImmutableExpression> newCondition = immutabilityTools.foldBooleanExpressions(
                Stream.concat(
                    Stream.of(updatedExistingCondition),
                    Stream.of(optionalNewEqualities))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(e -> e.flattenAND().stream()));

        return newCondition
                .map(cond -> createExpressionEvaluator()
                        .evaluateExpression(cond));
    }

    protected static ImmutableSet<Variable> union(ImmutableSet<Variable> set1, ImmutableSet<Variable> set2) {
        return Stream.concat(
                set1.stream(),
                set2.stream())
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        ImmutableMultiset<Variable> childrenVariableBag = query.getChildren(this).stream()
                .flatMap(c -> query.getVariables(c).stream())
                .collect(ImmutableCollectors.toMultiset());

        Stream<Variable> cooccuringVariableStream = childrenVariableBag.entrySet().stream()
                .filter(e -> e.getCount() > 1)
                .map(Multiset.Entry::getElement);

        return Stream.concat(cooccuringVariableStream, getLocallyRequiredVariables().stream())
                .collect(ImmutableCollectors.toSet());
    }

    protected ImmutabilityTools getImmutabilityTools() {
        return immutabilityTools;
    }

    /**
     * For children of a commutative join or for the left child of a LJ
     */
    protected <R> R liftRegularChildBinding(ConstructionNode selectedChildConstructionNode,
                                            int selectedChildPosition,
                                            IQTree selectedGrandChild,
                                            ImmutableList<IQTree> children,
                                            ImmutableSet<Variable> nonLiftableVariables,
                                            Optional<ImmutableExpression> initialJoiningCondition,
                                            VariableGenerator variableGenerator,
                                            LiftConverter<R> liftConverter) throws UnsatisfiableConditionException {

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

        ExpressionAndSubstitution expressionResults = simplifyCondition(
                computeNonOptimizedCondition(initialJoiningCondition, selectedChildSubstitution, freshRenaming),
                nonLiftableVariables);
        Optional<ImmutableExpression> newCondition = expressionResults.optionalExpression;

        ImmutableSubstitution<ImmutableTerm> ascendingSubstitution = expressionResults.substitution.composeWith(
                selectedChildSubstitution);
        ImmutableSubstitution<NonFunctionalTerm> descendingSubstitution =
                        expressionResults.substitution.composeWith2(freshRenaming)
                                .composeWith2(downPropagableFragment);

        return liftConverter.convert(children, selectedGrandChild, selectedChildPosition, newCondition,
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

        return getImmutabilityTools().foldBooleanExpressions(expressions);
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
    protected interface LiftConverter<R> {

        R convert(ImmutableList<IQTree> liftedChildren, IQTree selectedGrandChild, int selectedChildPosition,
                  Optional<ImmutableExpression> newCondition,
                  ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
                  ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution);
    }
}
