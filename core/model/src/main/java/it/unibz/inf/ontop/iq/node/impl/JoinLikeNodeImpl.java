package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
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
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;

public abstract class JoinLikeNodeImpl extends JoinOrFilterNodeImpl implements JoinLikeNode {

    private final ImmutabilityTools immutabilityTools;
    private final SubstitutionFactory substitutionFactory;

    protected JoinLikeNodeImpl(Optional<ImmutableExpression> optionalJoinCondition,
                               TermNullabilityEvaluator nullabilityEvaluator,
                               TermFactory termFactory,
                               TypeFactory typeFactory, DatalogTools datalogTools,
                               ExpressionEvaluator defaultExpressionEvaluator,
                               ImmutabilityTools immutabilityTools, SubstitutionFactory substitutionFactory) {
        super(optionalJoinCondition, nullabilityEvaluator, termFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator);
        this.immutabilityTools = immutabilityTools;
        this.substitutionFactory = substitutionFactory;
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
    protected <R> R liftSelectedChildBinding(ConstructionNode selectedChildConstructionNode,
                                             IQTree selectedGrandChild,
                                             ImmutableList<IQTree> otherChildren,
                                             Optional<ImmutableExpression> initialJoiningCondition,
                                             VariableGenerator variableGenerator,
                                             LiftConverter<R> liftConverter) throws UnsatisfiableJoiningConditionException {

        if (selectedChildConstructionNode.getOptionalModifiers().isPresent())
            throw new UnsupportedOperationException("Construction with query modifiers are" +
                    "currently not supported under a join");

        ImmutableSubstitution<ImmutableTerm> selectedChildSubstitution = selectedChildConstructionNode.getSubstitution();

        ImmutableSubstitution<VariableOrGroundTerm> downPropagableFragment = selectedChildSubstitution
                .getVariableOrGroundTermFragment();

        ImmutableSubstitution<NonGroundFunctionalTerm> nonDownPropagableFragment = selectedChildSubstitution
                .getNonGroundFunctionalTermFragment();


        ImmutableSet<Variable> otherChildrenVariables = otherChildren.stream()
                .flatMap(iq -> iq.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        InjectiveVar2VarSubstitution freshRenaming = computeOtherChildrenRenaming(nonDownPropagableFragment,
                otherChildrenVariables, variableGenerator);

        ExpressionAndSubstitution expressionResults = computeNewCondition(initialJoiningCondition,
                selectedChildSubstitution, freshRenaming);
        Optional<ImmutableExpression> newCondition = expressionResults.optionalExpression;

        ImmutableSubstitution<ImmutableTerm> ascendingSubstitution = expressionResults.substitution.composeWith(
                selectedChildSubstitution);
        ImmutableSubstitution<VariableOrGroundTerm> descendingSubstitution =
                (ImmutableSubstitution<VariableOrGroundTerm>)(ImmutableSubstitution<?>)
                        expressionResults.substitution.composeWith(freshRenaming)
                                .composeWith(downPropagableFragment);

        return liftConverter.convert(otherChildren, selectedGrandChild, newCondition, ascendingSubstitution,
                descendingSubstitution);
    }

    /**
     * TODO: Fixed point instead?
     */
    private ExpressionAndSubstitution convertIntoExpressionAndSubstitution(ImmutableExpression expression) {
        ImmutableSet<ImmutableExpression> expressions = expression.flattenAND();
        ImmutableSet<ImmutableExpression> substitutionExpressions = expressions.stream()
                .filter(e -> e.getFunctionSymbol().equals(EQ))
                .filter(e -> {
                    ImmutableList<? extends ImmutableTerm> arguments = e.getArguments();
                    return arguments.stream().allMatch(t -> t instanceof VariableOrGroundTerm)
                            && arguments.stream().anyMatch(t -> t instanceof Variable);
                })
                .collect(ImmutableCollectors.toSet());

        ImmutableMap<Variable, VariableOrGroundTerm> substitutionMap = substitutionExpressions.stream()
                .map(ImmutableFunctionalTerm::getArguments)
                .map(args -> (args.get(0) instanceof Variable) ? args : args.reverse())
                .collect(ImmutableCollectors.toMap(
                        args -> (Variable) args.get(0),
                        args -> (VariableOrGroundTerm) args.get(1)));

        ImmutableSubstitution<VariableOrGroundTerm> newSubstitution = substitutionFactory.getSubstitution(substitutionMap);

        Optional<ImmutableExpression> newExpression = getImmutabilityTools().foldBooleanExpressions(
                expressions.stream()
                        .filter(e -> !substitutionExpressions.contains(e)))
                .map(newSubstitution::applyToBooleanExpression);

        return new ExpressionAndSubstitution(newExpression, newSubstitution);
    }

    protected ExpressionAndSubstitution computeNewCondition(Optional<ImmutableExpression> initialJoiningCondition,
                                                          ImmutableSubstitution<ImmutableTerm> childSubstitution,
                                                          InjectiveVar2VarSubstitution freshRenaming)
            throws UnsatisfiableJoiningConditionException {

        Stream<ImmutableExpression> expressions = Stream.concat(
                initialJoiningCondition
                        .map(childSubstitution::applyToBooleanExpression)
                        .map(ImmutableExpression::flattenAND)
                        .orElseGet(ImmutableSet::of)
                        .stream(),
                freshRenaming.getImmutableMap().entrySet().stream()
                        .map(r -> termFactory.getImmutableExpression(EQ,
                                childSubstitution.applyToVariable(r.getKey()),
                                r.getValue())));

        Optional<ExpressionEvaluator.EvaluationResult> optionalEvaluationResults =
                getImmutabilityTools().foldBooleanExpressions(expressions)
                        .map(e -> createExpressionEvaluator().evaluateExpression(e));

        if (optionalEvaluationResults.isPresent()) {
            ExpressionEvaluator.EvaluationResult results = optionalEvaluationResults.get();

            if (results.isEffectiveFalse())
                throw new UnsatisfiableJoiningConditionException();

            return results.getOptionalExpression()
                    .map(this::convertIntoExpressionAndSubstitution)
                    .orElseGet(() ->
                            new ExpressionAndSubstitution(Optional.empty(), substitutionFactory.getSubstitution()));
        }
        else
            return new ExpressionAndSubstitution(Optional.empty(), substitutionFactory.getSubstitution());
    }

    private InjectiveVar2VarSubstitution computeOtherChildrenRenaming(ImmutableSubstitution<NonGroundFunctionalTerm> nonDownPropagableFragment,
                                                                      ImmutableSet<Variable> otherChildrenVariables,
                                                                      VariableGenerator variableGenerator) {
        ImmutableMap<Variable, Variable> substitutionMap = nonDownPropagableFragment.getImmutableMap().keySet().stream()
                .filter(otherChildrenVariables::contains)
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        variableGenerator::generateNewVariableFromVar));
        return substitutionFactory.getInjectiveVar2VarSubstitution(substitutionMap);
    }



    protected static class ExpressionAndSubstitution {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public final Optional<ImmutableExpression> optionalExpression;
        public final ImmutableSubstitution<VariableOrGroundTerm> substitution;

        private ExpressionAndSubstitution(Optional<ImmutableExpression> optionalExpression,
                                          ImmutableSubstitution<VariableOrGroundTerm> substitution) {
            this.optionalExpression = optionalExpression;
            this.substitution = substitution;
        }
    }

    protected static class UnsatisfiableJoiningConditionException extends Exception {
    }


    @FunctionalInterface
    protected interface LiftConverter<R> {

        R convert(ImmutableList<IQTree> otherLiftedChildren, IQTree selectedGrandChild,
                  Optional<ImmutableExpression> newCondition,
                  ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
                  ImmutableSubstitution<VariableOrGroundTerm> descendingSubstitution);
    }
}
