package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
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

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class JoinLikeNodeImpl extends JoinOrFilterNodeImpl implements JoinLikeNode {

    private final ImmutabilityTools immutabilityTools;
    private final SubstitutionFactory substitutionFactory;
    protected final ImmutableUnificationTools unificationTools;
    protected final ImmutableSubstitutionTools substitutionTools;

    protected JoinLikeNodeImpl(Optional<ImmutableExpression> optionalJoinCondition,
                               TermNullabilityEvaluator nullabilityEvaluator,
                               TermFactory termFactory,
                               TypeFactory typeFactory, DatalogTools datalogTools,
                               ExpressionEvaluator defaultExpressionEvaluator,
                               ImmutabilityTools immutabilityTools, SubstitutionFactory substitutionFactory,
                               ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools) {
        super(optionalJoinCondition, nullabilityEvaluator, termFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator);
        this.immutabilityTools = immutabilityTools;
        this.substitutionFactory = substitutionFactory;
        this.unificationTools = unificationTools;
        this.substitutionTools = substitutionTools;
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
                                            LiftConverter<R> liftConverter) throws UnsatisfiableJoiningConditionException {

        if (selectedChildConstructionNode.getOptionalModifiers().isPresent())
            throw new UnsupportedOperationException("Construction with query modifiers are" +
                    "currently not supported under a join");

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

    protected ExpressionAndSubstitution simplifyCondition(Optional<ImmutableExpression> nonOptimizedExpression,
                                                          ImmutableSet<Variable> nonLiftableVariables)
            throws UnsatisfiableJoiningConditionException {

        Optional<ExpressionEvaluator.EvaluationResult> optionalEvaluationResults =
                        nonOptimizedExpression
                        .map(e -> createExpressionEvaluator().evaluateExpression(e));

        if (optionalEvaluationResults.isPresent()) {
            ExpressionEvaluator.EvaluationResult results = optionalEvaluationResults.get();

            if (results.isEffectiveFalse())
                throw new UnsatisfiableJoiningConditionException();

            Optional<ImmutableExpression> optionalExpression = results.getOptionalExpression();
            if (optionalExpression.isPresent())
                // May throw an exception if unification is rejected
                return convertIntoExpressionAndSubstitution(optionalExpression.get(), nonLiftableVariables);
            else
                return new ExpressionAndSubstitution(Optional.empty(), substitutionFactory.getSubstitution());
        }
        else
            return new ExpressionAndSubstitution(Optional.empty(),
                    substitutionFactory.getSubstitution());
    }

    /**
     * TODO: explain
     *
     * Functional terms remain in the expression (never going into the substitution)
     *
     */
    private ExpressionAndSubstitution convertIntoExpressionAndSubstitution(ImmutableExpression expression,
                                                                           ImmutableSet<Variable> nonLiftableVariables)
            throws UnsatisfiableJoiningConditionException {

        ImmutableSet<ImmutableExpression> expressions = expression.flattenAND();
        ImmutableSet<ImmutableExpression> functionFreeEqualities = expressions.stream()
                .filter(e -> e.getFunctionSymbol().equals(EQ))
                .filter(e -> {
                    ImmutableList<? extends ImmutableTerm> arguments = e.getArguments();
                    return arguments.stream().allMatch(t -> t instanceof NonFunctionalTerm);
                })
                .collect(ImmutableCollectors.toSet());

        ImmutableSubstitution<NonFunctionalTerm> normalizedUnifier = unify(functionFreeEqualities.stream()
                .map(ImmutableFunctionalTerm::getArguments)
                .map(args -> Maps.immutableEntry(
                        (NonFunctionalTerm) args.get(0),
                        (NonFunctionalTerm)args.get(1))),
                nonLiftableVariables);

        Optional<ImmutableExpression> newExpression = getImmutabilityTools().foldBooleanExpressions(
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

        return new ExpressionAndSubstitution(newExpression, normalizedUnifier);
    }

    private ImmutableSubstitution<NonFunctionalTerm> unify(
            Stream<Map.Entry<NonFunctionalTerm, NonFunctionalTerm>> equalityStream,
            ImmutableSet<Variable> nonLiftableVariables) throws UnsatisfiableJoiningConditionException {
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
                .orElseThrow(UnsatisfiableJoiningConditionException::new);
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

    protected static class UnsatisfiableJoiningConditionException extends Exception {
    }


    @FunctionalInterface
    protected interface LiftConverter<R> {

        R convert(ImmutableList<IQTree> liftedChildren, IQTree selectedGrandChild, int selectedChildPosition,
                  Optional<ImmutableExpression> newCondition,
                  ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
                  ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution);
    }
}
