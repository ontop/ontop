package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.AscendingSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.AscendingSubstitutionNormalizer.AscendingSubstitutionNormalization;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.normalization.InnerJoinNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class InnerJoinNormalizerImpl implements InnerJoinNormalizer {

    private static final int MAX_ITERATIONS = 100000;
    private final JoinLikeChildBindingLifter bindingLift;
    private final IntermediateQueryFactory iqFactory;
    private final AscendingSubstitutionNormalizer substitutionNormalizer;
    private final ConditionSimplifier conditionSimplifier;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private InnerJoinNormalizerImpl(JoinLikeChildBindingLifter bindingLift, IntermediateQueryFactory iqFactory,
                                    AscendingSubstitutionNormalizer substitutionNormalizer,
                                    ConditionSimplifier conditionSimplifier, ImmutabilityTools immutabilityTools) {
        this.bindingLift = bindingLift;
        this.iqFactory = iqFactory;
        this.substitutionNormalizer = substitutionNormalizer;
        this.conditionSimplifier = conditionSimplifier;
        this.immutabilityTools = immutabilityTools;
    }

    @Override
    public IQTree normalizeForOptimization(InnerJoinNode innerJoinNode, ImmutableList<IQTree> children,
                                           VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        // Non-final
        State state = new State(children, innerJoinNode.getOptionalFilterCondition(), variableGenerator);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            state = state
                    .propagateDownCondition()
                    .liftChildBinding()
                    .liftDistincts()
                    .liftConditionAndMergeJoins();

            if (state.hasConverged())
                return state.createNormalizedTree(currentIQProperties);
        }

        throw new MinorOntopInternalBugException("InnerJoin.liftBinding() did not converge after " + MAX_ITERATIONS);
    }

    private static ImmutableSet<Variable> extractProjectedVariables(ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private class State {
        private final ImmutableSet<Variable> projectedVariables;
        // Parent first
        private final ImmutableList<UnaryOperatorNode> ancestors;
        private final ImmutableList<IQTree> children;
        private final Optional<ImmutableExpression> joiningCondition;
        private final boolean hasConverged;
        private final VariableGenerator variableGenerator;

        private State(ImmutableSet<Variable> projectedVariables,
                      ImmutableList<UnaryOperatorNode> ancestors, ImmutableList<IQTree> children,
                      Optional<ImmutableExpression> joiningCondition, VariableGenerator variableGenerator,
                      boolean hasConverged) {
            this.projectedVariables = projectedVariables;
            this.ancestors = ancestors;
            this.children = children;
            this.joiningCondition = joiningCondition;
            this.variableGenerator = variableGenerator;
            this.hasConverged = hasConverged;
        }

        /**
         * Initial constructor
         */
        public State(ImmutableList<IQTree> children, Optional<ImmutableExpression> joiningCondition,
                     VariableGenerator variableGenerator) {
            this(extractProjectedVariables(children), ImmutableList.of(), children,
                    joiningCondition, variableGenerator, false);
        }

        private State updateChildren(ImmutableList<IQTree> newChildren, boolean hasConverged) {
            return new State(projectedVariables, ancestors, newChildren, joiningCondition, variableGenerator, hasConverged);
        }

        private State updateConditionAndChildren(Optional<ImmutableExpression> newCondition,
                                                 ImmutableList<IQTree> newChildren) {
            return new State(projectedVariables, ancestors, newChildren, newCondition, variableGenerator, hasConverged);
        }

        private State updateParentConditionAndChildren(UnaryOperatorNode newParent, Optional<ImmutableExpression> newCondition,
                                                       ImmutableList<IQTree> newChildren) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .add(newParent)
                    .addAll(ancestors)
                    .build();

            return new State(projectedVariables, newAncestors, newChildren, newCondition, variableGenerator, false);
        }

        /**
         * No child is interpreted as EMPTY
         */
        private State declareAsEmpty() {
            EmptyNode emptyChild = iqFactory.createEmptyNode(projectedVariables);

            return new State(projectedVariables, ImmutableList.of(), ImmutableList.of(emptyChild),
                    Optional.empty(), variableGenerator, true);
        }

        /**
         * Lifts the binding OF AT MOST ONE child
         *
         * TODO: refactor
         */
        public State liftChildBinding() {
            ImmutableList<IQTree> liftedChildren = children.stream()
                    .map(c -> c.normalizeForOptimization(variableGenerator))
                    .filter(c -> !(c.getRootNode() instanceof TrueNode))
                    .collect(ImmutableCollectors.toList());

            if (liftedChildren.stream()
                    .anyMatch(IQTree::isDeclaredAsEmpty))
                return declareAsEmpty();


            OptionalInt optionalSelectedLiftedChildPosition = IntStream.range(0, liftedChildren.size())
                    .filter(i -> liftedChildren.get(i).getRootNode() instanceof ConstructionNode)
                    .findFirst();

            /*
             * No substitution to lift -> converged
             */
            if (!optionalSelectedLiftedChildPosition.isPresent())
                return updateChildren(liftedChildren, true);

            int selectedChildPosition = optionalSelectedLiftedChildPosition.getAsInt();
            UnaryIQTree selectedLiftedChild = (UnaryIQTree) liftedChildren.get(selectedChildPosition);

            ConstructionNode selectedChildConstructionNode = (ConstructionNode) selectedLiftedChild.getRootNode();
            IQTree selectedGrandChild = selectedLiftedChild.getChild();

            try {
                return bindingLift.liftRegularChildBinding(selectedChildConstructionNode,
                        selectedChildPosition,
                        selectedGrandChild,
                        liftedChildren, ImmutableSet.of(), joiningCondition, variableGenerator,
                        this::convertIntoState);
            } catch (UnsatisfiableConditionException e) {
                return declareAsEmpty();
            }
        }

        private State convertIntoState(
                ImmutableList<IQTree> liftedChildren, IQTree selectedGrandChild, int selectedChildPosition,
                Optional<ImmutableExpression> newCondition, ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
                ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {

            AscendingSubstitutionNormalization normalization = substitutionNormalizer
                    .normalizeAscendingSubstitution(ascendingSubstitution, extractProjectedVariables(liftedChildren));

            Optional<ConstructionNode> newParent = normalization.generateTopConstructionNode();

            ImmutableList<IQTree> newChildren = IntStream.range(0, liftedChildren.size())
                    .boxed()
                    .map(i -> i == selectedChildPosition
                            ? selectedGrandChild.applyDescendingSubstitution(descendingSubstitution, newCondition)
                            : liftedChildren.get(i).applyDescendingSubstitution(descendingSubstitution, newCondition))
                    .map(normalization::updateChild)
                    .collect(ImmutableCollectors.toList());

            return newParent
                    .map(p -> updateParentConditionAndChildren(p, newCondition, newChildren))
                    .orElseGet(() -> updateConditionAndChildren(newCondition, newChildren));
        }

        public IQTree createNormalizedTree(IQProperties currentIQProperties) {
            IQProperties normalizedIQProperties = currentIQProperties.declareNormalizedForOptimization();

            IQTree joinLevelTree = createJoinOrFilterOrEmpty(normalizedIQProperties);

            if (joinLevelTree.isDeclaredAsEmpty())
                return joinLevelTree;

            IQTree ancestorTree = ancestors.stream()
                    .reduce(joinLevelTree, (t, n) -> iqFactory.createUnaryIQTree(n, t),
                            // Should not be called
                            (t1, t2) -> {
                                throw new MinorOntopInternalBugException("The order must be respected");
                            });

            IQTree nonNormalizedTree = ancestorTree.getVariables().equals(projectedVariables)
                    ? ancestorTree
                    : iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(projectedVariables), ancestorTree);

            // Normalizes the ancestors (recursive)
            return nonNormalizedTree.normalizeForOptimization(variableGenerator);
        }


        private IQTree createJoinOrFilterOrEmpty(IQProperties normalizedIQProperties) {
            switch (children.size()) {
                case 0:
                    return iqFactory.createTrueNode();
                case 1:
                    IQTree uniqueChild = children.get(0);
                    return joiningCondition
                            .map(e -> (IQTree) iqFactory.createUnaryIQTree(iqFactory.createFilterNode(e), uniqueChild))
                            .orElse(uniqueChild);
                default:
                    InnerJoinNode newJoinNode = iqFactory.createInnerJoinNode(joiningCondition);
                    return iqFactory.createNaryIQTree(newJoinNode, children,
                            normalizedIQProperties);
            }
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
                        joiningCondition.get());

                Optional<ImmutableExpression> newJoiningCondition = conditionSimplificationResults.getOptionalExpression();
                // TODO: build a proper constraint (more than just the joining condition)

                ImmutableList<IQTree> newChildren = Optional.of(conditionSimplificationResults.getSubstitution())
                        .filter(s -> !s.isEmpty())
                        .map(s -> children.stream()
                                .map(child -> child.applyDescendingSubstitution(s, newJoiningCondition))
                                .collect(ImmutableCollectors.toList()))
                        .orElseGet(() -> newJoiningCondition
                                .map(s -> children.stream()
                                        .map(child -> child.propagateDownConstraint(s))
                                        .collect(ImmutableCollectors.toList()))
                                .orElse(children));

                Optional<ConstructionNode> newParent = Optional.of(conditionSimplificationResults.getSubstitution())
                        .filter(s -> !s.isEmpty())
                        .map(s -> iqFactory.createConstructionNode(extractProjectedVariables(children),
                                (ImmutableSubstitution<ImmutableTerm>) (ImmutableSubstitution<?>) s));

                return newParent
                        .map(p -> updateParentConditionAndChildren(p, newJoiningCondition, newChildren))
                        .orElseGet(() -> updateConditionAndChildren(newJoiningCondition, newChildren));

            } catch (UnsatisfiableConditionException e) {
                return declareAsEmpty();
            }
        }

        public boolean hasConverged() {
            return hasConverged;
        }

        public State liftDistincts() {
            Optional<DistinctNode> distinctNode = children.stream()
                    .filter(c -> c.getRootNode() instanceof DistinctNode)
                    .map(c -> (DistinctNode) c.getRootNode())
                    .findFirst();

            if (distinctNode.isPresent() && children.stream().allMatch(IQTree::isDistinct)) {
                DistinctNode newParent = distinctNode.get();

                ImmutableList<IQTree> newChildren = children.stream()
                        .map(IQTree::removeDistincts)
                        .collect(ImmutableCollectors.toList());

                return updateParentConditionAndChildren(newParent, joiningCondition, newChildren);
            }
            else
                return this;
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
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(c -> c.flattenAND().stream());

            Optional<ImmutableExpression> newJoiningCondition = immutabilityTools.foldBooleanExpressions(joiningCondition
                    .map(c -> Stream.concat(c.flattenAND().stream(), conditions))
                    .orElse(conditions));

            ImmutableList<IQTree> newChildren = conditionAndTrees.stream()
                    .flatMap(ct -> ct.trees)
                    .collect(ImmutableCollectors.toList());

            return updateConditionAndChildren(newJoiningCondition, newChildren);
        }

        private ConditionAndTrees extractConditionAndSubtrees(IQTree tree) {
            QueryNode rootNode = tree.getRootNode();

            if (rootNode instanceof CommutativeJoinNode) {
                CommutativeJoinNode joinNode = (CommutativeJoinNode) rootNode;
                return joinNode.getOptionalFilterCondition()
                        .map(c -> new ConditionAndTrees(c, tree.getChildren().stream()))
                        .orElseGet(() -> new ConditionAndTrees(tree.getChildren().stream()));

            } else if (rootNode instanceof FilterNode) {
                return new ConditionAndTrees(((FilterNode)rootNode).getFilterCondition(), tree.getChildren().stream());

            } else
                return new ConditionAndTrees(Stream.of(tree));

        }

    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class ConditionAndTrees {
        final Optional<ImmutableExpression> condition;
        final Stream<IQTree> trees;

        ConditionAndTrees(ImmutableExpression condition, Stream<IQTree> trees) {
            this.condition = Optional.of(condition);
            this.trees = trees;
        }

        ConditionAndTrees(Stream<IQTree> trees) {
            this.condition = Optional.empty();
            this.trees = trees;
        }
    }

}
