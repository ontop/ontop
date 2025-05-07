package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.normalization.InnerJoinNormalizer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


public class InnerJoinNormalizerImpl implements InnerJoinNormalizer {

    private static final int MAX_ITERATIONS = 10000;
    private static final int BINDING_LIFT_ITERATIONS = 1000;

    private final JoinLikeChildBindingLifter bindingLift;
    private final IntermediateQueryFactory iqFactory;
    private final ConstructionSubstitutionNormalizer substitutionNormalizer;
    private final ConditionSimplifier conditionSimplifier;
    private final TermFactory termFactory;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;

    private final IQTreeTools iqTreeTools;

    @Inject
    private InnerJoinNormalizerImpl(JoinLikeChildBindingLifter bindingLift, IntermediateQueryFactory iqFactory,
                                    ConstructionSubstitutionNormalizer substitutionNormalizer,
                                    ConditionSimplifier conditionSimplifier, TermFactory termFactory,
                                    JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                    IQTreeTools iqTreeTools) {
        this.bindingLift = bindingLift;
        this.iqFactory = iqFactory;
        this.substitutionNormalizer = substitutionNormalizer;
        this.conditionSimplifier = conditionSimplifier;
        this.termFactory = termFactory;
        this.variableNullabilityTools = variableNullabilityTools;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQTree normalizeForOptimization(InnerJoinNode innerJoinNode, ImmutableList<IQTree> children,
                                           VariableGenerator variableGenerator, IQTreeCache treeCache) {
        // Non-final
        State state = new State(children, innerJoinNode.getOptionalFilterCondition(), variableGenerator);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            State newState = liftBindingsAndDistincts(state)
                    // Removes the child construction nodes that were just projecting away irrelevant variables
                    .liftChildProjectingAwayConstructionNodes()
                    .liftConditionAndMergeJoins();

            if (newState.equals(state))
                return newState.liftLeftJoinAndCreateNormalizedTree(treeCache);
            state = newState;
        }

        throw new MinorOntopInternalBugException("InnerJoin.liftBinding() did not converge after " + MAX_ITERATIONS);
    }

    /**
     * Lifts bindings but children still project away irrelevant variables
     * (needed for limiting as much as possible the number of variables on which DISTINCT is applied)
     *
     * NB: Note that this number is not guaranteed to be minimal. However, it is guaranteed to be sound.
     */
    private State liftBindingsAndDistincts(State initialState) {

        // Non-final
        State state = initialState;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            State newState = state
                    .propagateDownCondition()
                    .liftBindings()
                    .liftDistincts();

            if (newState.equals(state))
                return newState;
            state = newState;
        }

        throw new MinorOntopInternalBugException("InnerJoin.liftBinding() did not converge after " + MAX_ITERATIONS);
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private class State {
        private final ImmutableSet<Variable> projectedVariables;
        // Parent first
        private final ImmutableList<UnaryOperatorNode> ancestors;
        private final ImmutableList<IQTree> children;
        private final Optional<ImmutableExpression> joiningCondition;
        private final VariableGenerator variableGenerator;
        private final VariableNullability childrenVariableNullability;

        private State(ImmutableSet<Variable> projectedVariables,
                      ImmutableList<UnaryOperatorNode> ancestors, ImmutableList<IQTree> children,
                      Optional<ImmutableExpression> joiningCondition, VariableGenerator variableGenerator,
                      VariableNullability childrenVariableNullability) {
            this.projectedVariables = projectedVariables;
            this.ancestors = ancestors;
            this.children = children;
            this.joiningCondition = joiningCondition;
            this.variableGenerator = variableGenerator;
            this.childrenVariableNullability = childrenVariableNullability;
        }

        /**
         * Initial constructor
         */
        public State(ImmutableList<IQTree> children, Optional<ImmutableExpression> joiningCondition,
                     VariableGenerator variableGenerator) {
            this(iqTreeTools.getChildrenVariables(children), ImmutableList.of(), children,
                    joiningCondition, variableGenerator,
                    variableNullabilityTools.getChildrenVariableNullability(children));
        }

        private State updateChildren(ImmutableList<IQTree> newChildren) {
            if (children.equals(newChildren))
                return this;
            return new State(projectedVariables, ancestors, newChildren, joiningCondition, variableGenerator,
                    variableNullabilityTools.getChildrenVariableNullability(newChildren));
        }

        private State updateConditionAndChildren(Optional<ImmutableExpression> newCondition,
                                                 ImmutableList<IQTree> newChildren) {
            return new State(projectedVariables, ancestors, newChildren, newCondition, variableGenerator,
                    variableNullabilityTools.getChildrenVariableNullability(newChildren));
        }

        private State updateParentConditionAndChildren(UnaryOperatorNode newParent, Optional<ImmutableExpression> newCondition,
                                                       ImmutableList<IQTree> newChildren) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .add(newParent)
                    .addAll(ancestors)
                    .build();

            return new State(projectedVariables, newAncestors, newChildren, newCondition, variableGenerator,
                    variableNullabilityTools.getChildrenVariableNullability(newChildren));
        }

        /**
         * No child is interpreted as EMPTY
         */
        private State declareAsEmpty() {
            EmptyNode emptyChild = iqFactory.createEmptyNode(projectedVariables);

            return new State(projectedVariables, ImmutableList.of(), ImmutableList.of(emptyChild),
                    Optional.empty(), variableGenerator, childrenVariableNullability);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o instanceof State) {
                State that = (State) o;
                return joiningCondition.equals(that.joiningCondition)
                        && children.equals(that.children)
                        && ancestors.equals(that.ancestors)
                        && projectedVariables.equals(that.projectedVariables);
            }
            return false;
        }


        public State liftBindings() {
            // Non-final
            State state = this;

            for (int i = 0; i < BINDING_LIFT_ITERATIONS; i++) {
                State newState = state.liftChildBinding();

                if (newState.equals(state))
                    return newState;
                state = newState;
            }
            return state;
        }

        /**
         * Lifts the binding OF AT MOST ONE child
         * 
         */
        private State liftChildBinding() {
            ImmutableList<IQTree> liftedChildren = children.stream()
                    .map(c -> c.normalizeForOptimization(variableGenerator))
                    .filter(c -> !(c.getRootNode() instanceof TrueNode))
                    .collect(ImmutableCollectors.toList());

            if (liftedChildren.stream()
                    .anyMatch(IQTree::isDeclaredAsEmpty))
                return declareAsEmpty();


            OptionalInt optionalSelectedLiftedChildPosition = IntStream.range(0, liftedChildren.size())
                    .filter(i -> UnaryIQTreeDecomposition.of(liftedChildren.get(i), ConstructionNode.class)
                            .getOptionalNode()
                            .map(ConstructionNode::getSubstitution)
                            .filter(s -> !s.isEmpty())
                            .isPresent())
                    .findFirst();

            /*
             * No substitution to lift
             */
            if (optionalSelectedLiftedChildPosition.isEmpty())
                return updateChildren(liftedChildren);

            int selectedChildPosition = optionalSelectedLiftedChildPosition.getAsInt();
            var selectedLiftedChild = UnaryIQTreeDecomposition.of(liftedChildren.get(selectedChildPosition), ConstructionNode.class);

            ConstructionNode selectedChildConstructionNode = selectedLiftedChild.get();
            IQTree selectedGrandChild = selectedLiftedChild.getChild();

            ImmutableSet<Variable> requiredGrandChildVariables = selectedChildConstructionNode.getChildVariables();

            IQTree selectedGrandChildWithLimitedProjection =
                    iqTreeTools.createConstructionNodeTreeIfNontrivial(selectedGrandChild, requiredGrandChildVariables);

            VariableNullability newChildrenVariableNullability = variableNullabilityTools.getChildrenVariableNullability(
                    IntStream.range(0, liftedChildren.size())
                            .mapToObj(i -> i == selectedChildPosition ? selectedGrandChildWithLimitedProjection : liftedChildren.get(i))
                            .collect(ImmutableCollectors.toList()));

            try {
                return bindingLift.liftRegularChildBinding(selectedChildConstructionNode,
                        selectedChildPosition,
                        selectedGrandChildWithLimitedProjection,
                        liftedChildren, ImmutableSet.of(), joiningCondition, variableGenerator,
                        newChildrenVariableNullability, this::convertIntoState);
            }
            catch (UnsatisfiableConditionException e) {
                return declareAsEmpty();
            }
        }

        private State convertIntoState(
                ImmutableList<IQTree> liftedChildren, IQTree selectedGrandChild, int selectedChildPosition,
                Optional<ImmutableExpression> notNormalizedCondition, Substitution<ImmutableTerm> ascendingSubstitution,
                Substitution<? extends VariableOrGroundTerm> descendingSubstitution) {

            ConstructionSubstitutionNormalization normalization = substitutionNormalizer
                    .normalizeSubstitution(ascendingSubstitution, iqTreeTools.getChildrenVariables(liftedChildren));

            Optional<ImmutableExpression> newCondition = notNormalizedCondition
                    .map(normalization::updateExpression);

            Optional<ConstructionNode> newParent = normalization.generateTopConstructionNode();

            ImmutableList<IQTree> newChildren = IntStream.range(0, liftedChildren.size())
                    .mapToObj(i -> i == selectedChildPosition
                            ? selectedGrandChild.applyDescendingSubstitution(descendingSubstitution, newCondition, variableGenerator)
                            : liftedChildren.get(i).applyDescendingSubstitution(descendingSubstitution, newCondition, variableGenerator))
                    .map(c -> normalization.updateChild(c, variableGenerator))
                    .collect(ImmutableCollectors.toList());

            return newParent
                    .map(p -> updateParentConditionAndChildren(p, newCondition, newChildren))
                    .orElseGet(() -> updateConditionAndChildren(newCondition, newChildren));
        }

        public IQTree liftLeftJoinAndCreateNormalizedTree(IQTreeCache treeCache) {
            IQTreeCache normalizedTreeCache = treeCache.declareAsNormalizedForOptimizationWithEffect();

            IQTree joinLevelTree = createJoinOrFilterOrEmptyOrLiftLeft(normalizedTreeCache);

            if (joinLevelTree.isDeclaredAsEmpty())
                return joinLevelTree;

            IQTree ancestorTree = iqTreeTools.createAncestorsUnaryIQTree(ancestors, joinLevelTree);

            IQTree nonNormalizedTree = iqTreeTools.createConstructionNodeTreeIfNontrivial(ancestorTree, projectedVariables);

            // Normalizes the ancestors (recursive)
            return nonNormalizedTree.normalizeForOptimization(variableGenerator);
        }

        /**
         * For safety (although conflicts are unlikely to appear)
         */
        private boolean isLeftJoinToLiftAboveJoin(int i) {
            IQTree currentChild = children.get(i);
            if (currentChild.getRootNode() instanceof LeftJoinNode) {
                BinaryNonCommutativeIQTree leftJoinTree = (BinaryNonCommutativeIQTree) currentChild;

                Set<Variable> rightSpecificVariables = Sets.difference(
                        leftJoinTree.getRightChild().getVariables(),
                        leftJoinTree.getLeftChild().getVariables());

                return IntStream.range(0, children.size())
                        .filter(j -> i != j)
                        .allMatch(j -> Sets.intersection(children.get(j).getVariables(), rightSpecificVariables).isEmpty());
            }
            return false;
        }


        private IQTree createJoinOrFilterOrEmptyOrLiftLeft(IQTreeCache normalizedTreeCache) {
            switch (children.size()) {
                case 0:
                    return iqFactory.createTrueNode();
                case 1:
                    IQTree uniqueChild = children.get(0);
                    return iqTreeTools.createOptionalUnaryIQTree(joiningCondition.map(iqFactory::createFilterNode), uniqueChild);
                default:
                    return liftLeftJoin()
                            .orElseGet(()-> iqFactory.createNaryIQTree(
                                    iqFactory.createInnerJoinNode(joiningCondition),
                                    children, normalizedTreeCache));
            }
        }

        /**
         * Puts the LJ above the inner join if possible
         */
        protected Optional<IQTree> liftLeftJoin() {
            OptionalInt ljChildToLiftIndex = IntStream.range(0, children.size())
                    .filter(this::isLeftJoinToLiftAboveJoin)
                    .findFirst();

            if (ljChildToLiftIndex.isEmpty())
                return Optional.empty();

            int index = ljChildToLiftIndex.getAsInt();
            BinaryNonCommutativeIQTree ljChild = (BinaryNonCommutativeIQTree) children.get(index);

            NaryIQTree newJoinOnLeft = iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(),
                    Stream.concat(
                            Stream.of(ljChild.getLeftChild()),
                            IntStream.range(0, children.size())
                                    .filter(i -> i != index)
                                    .mapToObj(children::get))
                            .collect(ImmutableCollectors.toList()));

            BinaryNonCommutativeIQTree newLeftJoinTree = iqFactory.createBinaryNonCommutativeIQTree(ljChild.getRootNode(), newJoinOnLeft,
                    ljChild.getRightChild());

            IQTree newTree = iqTreeTools.createOptionalUnaryIQTree(joiningCondition.map(iqFactory::createFilterNode), newLeftJoinTree);

            return Optional.of(newTree);
        }

        /**
         * TODO: collect the constraint
         */
        public State propagateDownCondition() {
            // TODO: consider that case as well
            if (!joiningCondition.isPresent())
                return this;

            try {

                ConditionSimplifier.ExpressionAndSubstitution conditionSimplificationResults = conditionSimplifier.simplifyCondition(
                        joiningCondition.get(), children, childrenVariableNullability);

                Optional<ImmutableExpression> newJoiningCondition = conditionSimplificationResults.getOptionalExpression();
                // TODO: build a proper constraint (more than just the joining condition)

                ImmutableList<IQTree> newChildren = Optional.of(conditionSimplificationResults.getSubstitution())
                        .filter(s -> !s.isEmpty())
                        .map(s -> children.stream()
                                .map(child -> child.applyDescendingSubstitution(s, newJoiningCondition, variableGenerator))
                                .collect(ImmutableCollectors.toList()))
                        .orElseGet(() -> newJoiningCondition
                                .map(s -> children.stream()
                                        .map(child -> child.propagateDownConstraint(s, variableGenerator))
                                        .collect(ImmutableCollectors.toList()))
                                .orElse(children));

                Optional<ConstructionNode> newParent = Optional.of(conditionSimplificationResults.getSubstitution())
                        .filter(s -> !s.isEmpty())
                        .map(s -> iqFactory.createConstructionNode(iqTreeTools.getChildrenVariables(children), s));

                return newParent
                        .map(p -> updateParentConditionAndChildren(p, newJoiningCondition, newChildren))
                        .orElseGet(() -> updateConditionAndChildren(newJoiningCondition, newChildren));

            } catch (UnsatisfiableConditionException e) {
                return declareAsEmpty();
            }
        }

        public State liftDistincts() {
            Optional<DistinctNode> distinctNode = children.stream()
                    .map(c -> UnaryIQTreeDecomposition.of(c, DistinctNode.class))
                    .map(UnaryIQTreeDecomposition::getOptionalNode)
                    .flatMap(Optional::stream)
                    .findFirst();

            if (distinctNode.isPresent() && isDistinct()) {
                DistinctNode newParent = distinctNode.get();

                ImmutableList<IQTree> newChildren = children.stream()
                        .map(IQTree::removeDistincts)
                        .collect(ImmutableCollectors.toList());

                return updateParentConditionAndChildren(newParent, joiningCondition, newChildren);
            }
            else
                return this;
        }

        private boolean isDistinct() {
            if (children.stream().allMatch(IQTree::isDistinct))
                return true;

            IQTree tree = iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(joiningCondition),
                    children);
            return tree.isDistinct();
        }


        public State liftConditionAndMergeJoins() {
            if (children.stream()
                    .noneMatch(c -> c.getRootNode() instanceof CommutativeJoinOrFilterNode))
                return this;

            ImmutableList<ConditionAndTrees> conditionAndTrees = children.stream()
                    .map(this::extractConditionAndSubtrees)
                    .collect(ImmutableCollectors.toList());

            Stream<ImmutableExpression> conditions = conditionAndTrees.stream()
                    .map(ct -> ct.condition)
                    .flatMap(Optional::stream);

            Optional<ImmutableExpression> newJoiningCondition = termFactory.getConjunction(joiningCondition, conditions);

            ImmutableList<IQTree> newChildren = conditionAndTrees.stream()
                    .flatMap(ct -> ct.trees)
                    .collect(ImmutableCollectors.toList());

            return updateConditionAndChildren(newJoiningCondition, newChildren);
        }

        private ConditionAndTrees extractConditionAndSubtrees(IQTree tree) {
            var filter = UnaryIQTreeDecomposition.of(tree, FilterNode.class);
            if (filter.isPresent())
                return new ConditionAndTrees(Optional.of(filter.get().getFilterCondition()), Stream.of(filter.getChild()));
            
            var join = IQTreeTools.NaryIQTreeDecomposition.of(tree, InnerJoinNode.class);
            if (join.isPresent())
                return new ConditionAndTrees(join.get().getOptionalFilterCondition(), join.getChildren().stream());

            return new ConditionAndTrees(Optional.empty(), Stream.of(tree));
        }

        /**
         * Gets rid of construction without substitution at the top of children
         */
        public State liftChildProjectingAwayConstructionNodes() {
            ImmutableList<IQTree> newChildren = children.stream()
                    .map(c -> UnaryIQTreeDecomposition.of(c, ConstructionNode.class)
                            .map((cn, st) -> cn.getSubstitution().isEmpty() ? st : c)
                            .orElse(c))
                    .collect(ImmutableCollectors.toList());

            if (newChildren.equals(children))
                return this;

            ImmutableSet<Variable> childrenVariables = iqTreeTools.getChildrenVariables(children);

            ConstructionNode newParent = iqFactory.createConstructionNode(childrenVariables);

            return updateParentConditionAndChildren(newParent, joiningCondition, newChildren);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class ConditionAndTrees {
        final Optional<ImmutableExpression> condition;
        final Stream<IQTree> trees;

        ConditionAndTrees(Optional<ImmutableExpression> condition, Stream<IQTree> trees) {
            this.condition = condition;
            this.trees = trees;
        }
    }

}
