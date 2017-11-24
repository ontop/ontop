package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.DefaultSubstitutionResults;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.NodeTransformationProposedState.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;

public class InnerJoinNodeImpl extends JoinLikeNodeImpl implements InnerJoinNode {

    private static final String JOIN_NODE_STR = "JOIN" ;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @AssistedInject
    protected InnerJoinNodeImpl(@Assisted Optional<ImmutableExpression> optionalFilterCondition,
                                TermNullabilityEvaluator nullabilityEvaluator,
                                TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                                ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                                IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        super(optionalFilterCondition, nullabilityEvaluator, termFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator, immutabilityTools);
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @AssistedInject
    private InnerJoinNodeImpl(@Assisted ImmutableExpression joiningCondition,
                              TermNullabilityEvaluator nullabilityEvaluator,
                              TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                              ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                              IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        super(Optional.of(joiningCondition), nullabilityEvaluator, termFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator, immutabilityTools);
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @AssistedInject
    private InnerJoinNodeImpl(TermNullabilityEvaluator nullabilityEvaluator, TermFactory termFactory,
                              TypeFactory typeFactory, DatalogTools datalogTools,
                              ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                              IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        super(Optional.empty(), nullabilityEvaluator, termFactory, typeFactory, datalogTools, defaultExpressionEvaluator,
                immutabilityTools);
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public InnerJoinNode clone() {
        return new InnerJoinNodeImpl(getOptionalFilterCondition(), getNullabilityEvaluator(),
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), getImmutabilityTools(), iqFactory, substitutionFactory);
    }

    @Override
    public InnerJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public InnerJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return new InnerJoinNodeImpl(newOptionalFilterCondition, getNullabilityEvaluator(),
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), getImmutabilityTools(), iqFactory, substitutionFactory);
    }

    @Override
    public SubstitutionResults<InnerJoinNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {

        if (substitution.isEmpty()) {
            return DefaultSubstitutionResults.noChange();
        }

        ImmutableSet<Variable> nullVariables = substitution.getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue().equals(termFactory.getNullConstant()))
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());


        ImmutableSet<Variable > otherNodesProjectedVariables = query.getOtherChildrenStream(this, childNode)
                .flatMap(c -> query.getVariables(c).stream())
                .collect(ImmutableCollectors.toSet());

        /*
         * If there is an implicit equality involving one null variables, the join is empty.
         */
        if (otherNodesProjectedVariables.stream()
                .anyMatch(nullVariables::contains)) {
            // Reject
            return DefaultSubstitutionResults.declareAsEmpty();
        }

        return computeAndEvaluateNewCondition(substitution, Optional.empty())
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> DefaultSubstitutionResults.noChange(substitution));
    }

    @Override
    public SubstitutionResults<InnerJoinNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {

        return getOptionalFilterCondition()
                .map(cond -> transformBooleanExpression(substitution, cond))
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> DefaultSubstitutionResults.noChange(substitution));
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {

        if (isFilteringNullValue(variable))
            return false;

        // Non-already
        boolean alsoProjectedByAnotherChild = false;

        for(QueryNode child : query.getChildren(this)) {
            if (query.getVariables(child).contains(variable)) {
                // Joining conditions cannot be null
                if (alsoProjectedByAnotherChild)
                    return false;

                if (child.isVariableNullable(query, variable))
                    alsoProjectedByAnotherChild = true;
                else
                    return false;
            }
        }

        if (!alsoProjectedByAnotherChild)
            throw new IllegalArgumentException("The variable " + variable + " is not projected by " + this);

        return true;
    }

    private SubstitutionResults<InnerJoinNode> applyEvaluation(ExpressionEvaluator.EvaluationResult evaluationResult,
                                                               ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        if (evaluationResult.isEffectiveFalse()) {
            return DefaultSubstitutionResults.declareAsEmpty();
        }
        else {
            InnerJoinNode newNode = changeOptionalFilterCondition(evaluationResult.getOptionalExpression());
            return DefaultSubstitutionResults.newNode(newNode, substitution);
        }
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof InnerJoinNode) &&
            this.getOptionalFilterCondition().equals(((InnerJoinNode) node).getOptionalFilterCondition());
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {

        return new NodeTransformationProposalImpl(NodeTransformationProposedState.DECLARE_AS_EMPTY,
                query.getVariables(this));
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueChild) {
        ImmutableList<QueryNode> remainingChildren = query.getChildrenStream(this)
                .filter(c -> c != trueChild)
                .collect(ImmutableCollectors.toList());
        switch (remainingChildren.size()) {
            case 0:
                return new NodeTransformationProposalImpl(DECLARE_AS_TRUE, ImmutableSet.of());
            case 1:
                return getOptionalFilterCondition()
                        .map(immutableExpression -> new NodeTransformationProposalImpl(REPLACE_BY_NEW_NODE,
                                query.getFactory().createFilterNode(immutableExpression),
                                ImmutableSet.of()))
                        .orElseGet(() -> new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_NON_EMPTY_CHILD,
                                remainingChildren.get(0), ImmutableSet.of()));
            default:
                return new NodeTransformationProposalImpl(NO_LOCAL_CHANGE, ImmutableSet.of());
        }
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return JOIN_NODE_STR + getOptionalFilterString();
    }

    /**
     * TODO: explain
     */
    @Override
    public IQTree liftBinding(ImmutableList<IQTree> initialChildren, VariableGenerator variableGenerator) {
        final ImmutableSet<Variable> projectedVariables = initialChildren.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        // Non-final
        ImmutableList<IQTree> currentChildren = initialChildren;
        ImmutableSubstitution<ImmutableTerm> currentSubstitution = substitutionFactory.getSubstitution();
        Optional<ImmutableExpression> currentJoiningCondition = getOptionalFilterCondition();
        boolean hasConverged = false;

        try {

            while (!hasConverged) {
                LiftingStepResults results = liftChildBinding(currentChildren, currentJoiningCondition, variableGenerator);
                hasConverged = results.hasConverged;
                currentChildren = results.children;
                currentSubstitution = results.substitution.composeWith(currentSubstitution);
                currentJoiningCondition = results.joiningCondition;
            }
            IQTree joinIQ = createJoinOrFilterOrTrue(currentChildren, currentJoiningCondition);

            ImmutableSubstitution<ImmutableTerm> ascendingSubstitution = substitutionFactory
                    .getSubstitution(currentSubstitution.getImmutableMap().entrySet().stream()
                            .filter(e -> projectedVariables.contains(e.getKey()))
                            .collect(ImmutableCollectors.toMap()));

            return ascendingSubstitution.isEmpty()
                    ? joinIQ
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(projectedVariables, ascendingSubstitution), joinIQ, true);

        } catch (EmptyIQException e) {
            return iqFactory.createEmptyNode(projectedVariables);
        }
    }

    /**
     * Lifts the binding OF AT MOST ONE child
     */
    private LiftingStepResults liftChildBinding(ImmutableList<IQTree> initialChildren,
                                                Optional<ImmutableExpression> initialJoiningCondition,
                                                VariableGenerator variableGenerator) throws EmptyIQException {

        ImmutableList<IQTree> liftedChildren = initialChildren.stream()
                .map(c -> c.liftBinding(variableGenerator))
                .filter(c -> !(c.getRootNode() instanceof TrueNode))
                .collect(ImmutableCollectors.toList());

        if (liftedChildren.stream()
                .anyMatch(iq -> iq.getRootNode() instanceof EmptyNode))
            throw new EmptyIQException();

        Optional<IQTree> optionalSelectedLiftedChild = liftedChildren.stream()
                .filter(iq -> iq.getRootNode() instanceof ConstructionNode)
                .findFirst();

        /*
         * No substitution to lift -> converged
         */
        if (!optionalSelectedLiftedChild.isPresent())
            return new LiftingStepResults(substitutionFactory.getSubstitution(), liftedChildren,
                    initialJoiningCondition, true);

        UnaryIQTree selectedLiftedChild = (UnaryIQTree) optionalSelectedLiftedChild.get();
        ConstructionNode selectedConstructionNode = (ConstructionNode) selectedLiftedChild.getRootNode();

        if (selectedConstructionNode.getOptionalModifiers().isPresent())
            throw new UnsupportedOperationException("Construction with query modifiers are" +
                    "currently not supported under a join");

        ImmutableSubstitution<ImmutableTerm> selectedChildSubstitution = selectedConstructionNode.getSubstitution();

        ImmutableSubstitution<VariableOrGroundTerm> downPropagableFragment = selectedChildSubstitution
                .getVariableOrGroundTermFragment();

        ImmutableSubstitution<NonGroundFunctionalTerm> nonDownPropagableFragment = selectedChildSubstitution
                .getNonGroundFunctionalTermFragment();


        ImmutableList<IQTree> otherInitialChildren = liftedChildren.stream()
                .filter(c -> c != selectedLiftedChild)
                .collect(ImmutableCollectors.toList());

        ImmutableSet<Variable> otherChildrenVariables = otherInitialChildren.stream()
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

        /*
         * TODO: should we try to preserve the children order?
         */
        ImmutableList<IQTree> newChildren = Stream.concat(
                otherInitialChildren.stream()
                        .map(c -> c.applyDescendingSubstitution(descendingSubstitution, newCondition)),
                Stream.of(selectedLiftedChild.getChild()))
                .collect(ImmutableCollectors.toList());

        return new LiftingStepResults(ascendingSubstitution, newChildren, newCondition, false);
    }

    private IQTree createJoinOrFilterOrTrue(ImmutableList<IQTree> currentChildren,
                                            Optional<ImmutableExpression> currentJoiningCondition) {
        switch (currentChildren.size()) {
            case 0:
                return iqFactory.createTrueNode();
            case 1:
                IQTree uniqueChild = currentChildren.get(0);
                return currentJoiningCondition
                        .map(e -> (IQTree) iqFactory.createUnaryIQTree(iqFactory.createFilterNode(e), uniqueChild))
                        .orElse(uniqueChild);
            default:
                InnerJoinNode newJoinNode = currentJoiningCondition.equals(getOptionalFilterCondition())
                        ? this
                        : changeOptionalFilterCondition(currentJoiningCondition);
                return iqFactory.createNaryIQTree(newJoinNode, currentChildren, true);
        }
    }

    private ExpressionAndSubstitution computeNewCondition(Optional<ImmutableExpression> initialJoiningCondition,
                                                          ImmutableSubstitution<ImmutableTerm> childSubstitution,
                                                          InjectiveVar2VarSubstitution freshRenaming)
            throws EmptyIQException {

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
                throw new EmptyIQException();

            return results.getOptionalExpression()
                    .map(this::convertIntoExpressionAndSubstitution)
                    .orElseGet(() ->
                            new ExpressionAndSubstitution(Optional.empty(), substitutionFactory.getSubstitution()));
        }
        else
            return new ExpressionAndSubstitution(Optional.empty(), substitutionFactory.getSubstitution());
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


    private static class LiftingStepResults {
        public final ImmutableSubstitution<ImmutableTerm> substitution;
        public final ImmutableList<IQTree> children;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public final Optional<ImmutableExpression> joiningCondition;
        public final boolean hasConverged;

        private LiftingStepResults(ImmutableSubstitution<ImmutableTerm> substitution, ImmutableList<IQTree> children,
                                   Optional<ImmutableExpression> joiningCondition, boolean hasConverged) {
            this.substitution = substitution;
            this.children = children;
            this.joiningCondition = joiningCondition;
            this.hasConverged = hasConverged;
        }
    }


    private static class ExpressionAndSubstitution {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public final Optional<ImmutableExpression> optionalExpression;
        public final ImmutableSubstitution<VariableOrGroundTerm> substitution;

        private ExpressionAndSubstitution(Optional<ImmutableExpression> optionalExpression,
                                          ImmutableSubstitution<VariableOrGroundTerm> substitution) {
            this.optionalExpression = optionalExpression;
            this.substitution = substitution;
        }
    }

    private static class EmptyIQException extends Exception {
    }
 }
