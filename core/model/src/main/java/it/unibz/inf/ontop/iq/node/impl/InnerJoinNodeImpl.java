package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
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
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.iq.node.NodeTransformationProposedState.*;

public class InnerJoinNodeImpl extends JoinLikeNodeImpl implements InnerJoinNode {

    private static final String JOIN_NODE_STR = "JOIN" ;
    private static final int MAX_ITERATIONS = 100000;
    private final ConstructionNodeTools constructionNodeTools;

    @AssistedInject
    protected InnerJoinNodeImpl(@Assisted Optional<ImmutableExpression> optionalFilterCondition,
                                TermNullabilityEvaluator nullabilityEvaluator,
                                TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                                ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                                IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                                ConstructionNodeTools constructionNodeTools,
                                ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools) {
        super(optionalFilterCondition, nullabilityEvaluator, termFactory, iqFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator, immutabilityTools, substitutionFactory, unificationTools, substitutionTools);
        this.constructionNodeTools = constructionNodeTools;
    }

    @AssistedInject
    private InnerJoinNodeImpl(@Assisted ImmutableExpression joiningCondition,
                              TermNullabilityEvaluator nullabilityEvaluator,
                              TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                              ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                              IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                              ConstructionNodeTools constructionNodeTools,
                              ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools) {
        super(Optional.of(joiningCondition), nullabilityEvaluator, termFactory, iqFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator, immutabilityTools, substitutionFactory, unificationTools, substitutionTools);
        this.constructionNodeTools = constructionNodeTools;
    }

    @AssistedInject
    private InnerJoinNodeImpl(TermNullabilityEvaluator nullabilityEvaluator, TermFactory termFactory,
                              TypeFactory typeFactory, DatalogTools datalogTools,
                              ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                              IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                              ConstructionNodeTools constructionNodeTools,
                              ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools) {
        super(Optional.empty(), nullabilityEvaluator, termFactory, iqFactory, typeFactory, datalogTools, defaultExpressionEvaluator,
                immutabilityTools, substitutionFactory, unificationTools, substitutionTools);
        this.constructionNodeTools = constructionNodeTools;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public InnerJoinNode clone() {
        return new InnerJoinNodeImpl(getOptionalFilterCondition(), getNullabilityEvaluator(),
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), getImmutabilityTools(), iqFactory,
                substitutionFactory, constructionNodeTools, unificationTools, substitutionTools);
    }

    @Override
    public InnerJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public InnerJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return new InnerJoinNodeImpl(newOptionalFilterCondition, getNullabilityEvaluator(),
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), getImmutabilityTools(), iqFactory,
                substitutionFactory, constructionNodeTools, unificationTools, substitutionTools);
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
        return applyDescendingSubstitution(substitution);
    }

    private SubstitutionResults<InnerJoinNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {

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
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof InnerJoinNode)
                && getOptionalFilterCondition().equals(((InnerJoinNode) queryNode).getOptionalFilterCondition());
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
    public IQTree liftBinding(ImmutableList<IQTree> initialChildren, VariableGenerator variableGenerator,
                              IQProperties currentIQProperties) {
        IQTree newParentTree = propagateDownCondition(Optional.empty(), initialChildren);

        /*
         * If after propagating down the condition the root is still a join node, goes to the next step
         */
        if (newParentTree.getRootNode() instanceof InnerJoinNodeImpl) {
            return ((InnerJoinNodeImpl)newParentTree.getRootNode()).liftBindingAfterPropagatingCondition(
                    newParentTree.getChildren(), variableGenerator, currentIQProperties);
        }
        else
            /*
             * Otherwise, goes back to the general method
             */
            return newParentTree.liftBinding(variableGenerator);
    }

    /**
     * TODO: explain
     */
    private IQTree liftBindingAfterPropagatingCondition(ImmutableList<IQTree> initialChildren,
                                                        VariableGenerator variableGenerator,
                                                        IQProperties currentIQProperties) {
        final ImmutableSet<Variable> projectedVariables = initialChildren.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        // Non-final
        ImmutableList<IQTree> currentChildren = initialChildren;
        ImmutableSubstitution<ImmutableTerm> currentSubstitution = substitutionFactory.getSubstitution();
        Optional<ImmutableExpression> currentJoiningCondition = getOptionalFilterCondition();
        boolean hasConverged = false;

        try {

            int i = 0;
            while ((!hasConverged) && (i++ < MAX_ITERATIONS)) {
                LiftingStepResults results = liftChildBinding(currentChildren, currentJoiningCondition, variableGenerator);
                hasConverged = results.hasConverged;
                currentChildren = results.children;
                currentSubstitution = results.substitution.composeWith(currentSubstitution);
                currentJoiningCondition = results.joiningCondition;
            }

            if (i >= MAX_ITERATIONS)
                throw new MinorOntopInternalBugException("InnerJoin.liftBinding() did not converge after " + MAX_ITERATIONS);

            IQTree joinIQ = createJoinOrFilterOrTrue(currentChildren, currentJoiningCondition, currentIQProperties);

            AscendingSubstitutionNormalization ascendingNormalization =
                    normalizeAscendingSubstitution(currentSubstitution, projectedVariables);

            IQTree newJoinIQ = ascendingNormalization.normalizeChild(joinIQ);

            ImmutableSet<Variable> childrenVariables = currentChildren.stream()
                    .flatMap(c -> c.getVariables().stream())
                    .collect(ImmutableCollectors.toSet());

            /*
             * NB: creates a construction if a substitution needs to be propagated and/or if some variables
             * have to be projected away
             */
            return ascendingNormalization.generateTopConstructionNode()
                    .map(Optional::of)
                    .orElseGet(() -> Optional.of(projectedVariables)
                            .filter(vars -> !vars.equals(childrenVariables))
                            .map(iqFactory::createConstructionNode))
                    .map(constructionNode -> (IQTree) iqFactory.createUnaryIQTree(constructionNode, newJoinIQ,
                            currentIQProperties.declareLifted()))
                    .orElse(newJoinIQ);

        } catch (EmptyIQException e) {
            return iqFactory.createEmptyNode(projectedVariables);
        }
    }

    /**
     * TODO: consider the constraint
     * TODO: adopt the new style
     */
    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, ImmutableList<IQTree> children) {
        SubstitutionResults<InnerJoinNode> results = applyDescendingSubstitution(descendingSubstitution);

        InnerJoinNode joinNode;
        switch (results.getLocalAction()) {
            case NO_CHANGE:
                joinNode = this;
                break;
            case NEW_NODE:
                joinNode = results.getOptionalNewNode().get();
                break;
            case DECLARE_AS_EMPTY:
                return iqFactory.createEmptyNode(computeNewlyProjectedVariables(descendingSubstitution, children));
            default:
                throw new MinorOntopInternalBugException("Unexpected local action " +
                        "after applying a descending substitution to a inner join: " + results.getLocalAction());
        }

        ImmutableList<IQTree> updatedChildren = children.stream()
                .map(c -> c.applyDescendingSubstitution(descendingSubstitution, constraint))
                .filter(c -> !(c instanceof TrueNode))
                .collect(ImmutableCollectors.toList());

        if (updatedChildren.stream()
                .anyMatch(IQTree::isDeclaredAsEmpty)) {
            return iqFactory.createEmptyNode(computeNewlyProjectedVariables(descendingSubstitution, children));
        }

        switch (updatedChildren.size()) {
            case 0:
                return iqFactory.createTrueNode();
            case 1:
                return updatedChildren.get(0);
            default:
                return iqFactory.createNaryIQTree(joinNode, updatedChildren);
        }
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables(ImmutableList<IQTree> children) {
        ImmutableMap<Variable, Collection<IQTree>> variableProvenanceMap = children.stream()
                .flatMap(c -> c.getVariables().stream()
                        .map(v -> Maps.immutableEntry(v, c)))
                .collect(ImmutableCollectors.toMultimap())
                .asMap();

        return variableProvenanceMap.entrySet().stream()
                .filter(e -> e.getValue().size() == 1)
                .filter(e -> e.getValue().iterator().next().containsNullableVariable(e.getKey()))
                .map(Map.Entry::getKey)
                .filter(this::isFilteringNullValue)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public boolean isConstructed(Variable variable, ImmutableList<IQTree> children) {
        return children.stream()
                .anyMatch(c -> c.isConstructed(variable));
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, ImmutableList<IQTree> children) {
        return IntStream.range(0, children.size()).boxed()
                .map(i -> Maps.immutableEntry(i, children.get(i)))
                .filter(e -> e.getValue().isConstructed(variable))
                // index -> new child
                .map(e -> Maps.immutableEntry(e.getKey(), e.getValue().liftIncompatibleDefinitions(variable)))
                .filter(e -> {
                            QueryNode newRootNode = e.getValue().getRootNode();
                            return (newRootNode instanceof UnionNode)
                                    && ((UnionNode) newRootNode).hasAChildWithLiftableDefinition(variable,
                                    e.getValue().getChildren());
                })
                .findFirst()
                .map(e -> liftUnionChild(e.getKey(), (NaryIQTree) e.getValue(), children))
                .orElseGet(() -> iqFactory.createNaryIQTree(this, children));
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, ImmutableList<IQTree> children) {
        return propagateDownCondition(Optional.of(constraint), children);
    }

    private IQTree propagateDownCondition(Optional<ImmutableExpression> initialConstraint, ImmutableList<IQTree> children) {
        try {
            ExpressionAndSubstitution conditionSimplificationResults =
                    simplifyCondition(getOptionalFilterCondition(), ImmutableSet.of());

            Optional<ImmutableExpression> downConstraint = computeDownConstraint(initialConstraint,
                    conditionSimplificationResults);

            //TODO: propagate different constraints to different children

            ImmutableList<IQTree> newChildren = Optional.of(conditionSimplificationResults.substitution)
                    .filter(s -> !s.isEmpty())
                    .map(s -> children.stream()
                            .map(child -> child.applyDescendingSubstitution(s, downConstraint))
                            .collect(ImmutableCollectors.toList())
                    )
                    .orElseGet(() -> downConstraint
                            .map(s -> children.stream()
                                    .map(child -> child.propagateDownConstraint(s))
                                    .collect(ImmutableCollectors.toList()))
                            .orElse(children));

            InnerJoinNode newJoin = conditionSimplificationResults.optionalExpression.equals(getOptionalFilterCondition())
                    ? this
                    : conditionSimplificationResults.optionalExpression
                    .map(iqFactory::createInnerJoinNode)
                    .orElseGet(iqFactory::createInnerJoinNode);

            NaryIQTree joinTree = iqFactory.createNaryIQTree(newJoin, newChildren);

            return Optional.of(conditionSimplificationResults.substitution)
                    .filter(s -> !s.isEmpty())
                    .map(s -> iqFactory.createConstructionNode(children.stream()
                            .flatMap(c -> c.getVariables().stream())
                            .collect(ImmutableCollectors.toSet()),
                            (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)s))
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, joinTree))
                    .orElse(joinTree);

        } catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(children.stream()
                    .flatMap(c -> c.getVariables().stream())
                    .collect(ImmutableCollectors.toSet()));
        }
    }

    private IQTree liftUnionChild(int childIndex, NaryIQTree newUnionChild, ImmutableList<IQTree> initialChildren) {
        UnionNode newUnionNode = iqFactory.createUnionNode(initialChildren.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet()));

        return iqFactory.createNaryIQTree(newUnionNode,
                newUnionChild.getChildren().stream()
                        .map(unionGrandChild -> createJoinSubtree(childIndex, unionGrandChild, initialChildren))
                        .collect(ImmutableCollectors.toList()));
    }

    private IQTree createJoinSubtree(int childIndex, IQTree unionGrandChild, ImmutableList<IQTree> initialChildren) {
        return iqFactory.createNaryIQTree(this,
                IntStream.range(0, initialChildren.size())
                        .boxed()
                        .map(i -> i == childIndex
                                ? unionGrandChild
                                : initialChildren.get(i))
                        .collect(ImmutableCollectors.toList()));
    }

    private ImmutableSet<Variable> computeNewlyProjectedVariables(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            ImmutableList<IQTree> children) {
        ImmutableSet<Variable> formerProjectedVariables = children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        return constructionNodeTools.computeNewProjectedVariables(descendingSubstitution, formerProjectedVariables);
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
                .anyMatch(IQTree::isDeclaredAsEmpty))
            throw new EmptyIQException();


        OptionalInt optionalSelectedLiftedChildPosition = IntStream.range(0, liftedChildren.size())
                .filter(i -> liftedChildren.get(i).getRootNode() instanceof ConstructionNode)
                .findFirst();

        /*
         * No substitution to lift -> converged
         */
        if (!optionalSelectedLiftedChildPosition.isPresent())
            return new InnerJoinNodeImpl.LiftingStepResults(substitutionFactory.getSubstitution(), liftedChildren,
                    initialJoiningCondition, true);

        int selectedChildPosition = optionalSelectedLiftedChildPosition.getAsInt();
        UnaryIQTree selectedLiftedChild = (UnaryIQTree) liftedChildren.get(selectedChildPosition);

        ConstructionNode selectedChildConstructionNode = (ConstructionNode) selectedLiftedChild.getRootNode();
        IQTree selectedGrandChild = selectedLiftedChild.getChild();

        try {
            return liftRegularChildBinding(selectedChildConstructionNode,
                    selectedChildPosition,
                    selectedGrandChild,
                    liftedChildren, ImmutableSet.of(), initialJoiningCondition, variableGenerator,
                    this::convertIntoLiftingStepResults);
        } catch (UnsatisfiableConditionException e) {
            throw new EmptyIQException();
        }
    }

    /**
     * TODO: should we try to preserve the children order?
     */
    private LiftingStepResults convertIntoLiftingStepResults(
            ImmutableList<IQTree> liftedChildren, IQTree selectedGrandChild, int selectedChildPosition,
            Optional<ImmutableExpression> newCondition, ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        ImmutableList<IQTree> newChildren = IntStream.range(0, liftedChildren.size())
                .boxed()
                .map(i -> i == selectedChildPosition
                        ? selectedGrandChild.applyDescendingSubstitution(descendingSubstitution, newCondition)
                        : liftedChildren.get(i).applyDescendingSubstitution(descendingSubstitution, newCondition))
                .collect(ImmutableCollectors.toList());

        return new LiftingStepResults(ascendingSubstitution, newChildren, newCondition, false);
    }



    private IQTree createJoinOrFilterOrTrue(ImmutableList<IQTree> currentChildren,
                                            Optional<ImmutableExpression> currentJoiningCondition,
                                            IQProperties currentIQProperties) {
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
                return iqFactory.createNaryIQTree(newJoinNode, currentChildren, currentIQProperties.declareLifted());
        }
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


    private static class EmptyIQException extends Exception {
    }

 }
