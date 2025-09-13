package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.normalization.InnerJoinNormalizer;
import it.unibz.inf.ontop.iq.visit.impl.IQStateDefaultTransformer;
import it.unibz.inf.ontop.iq.visit.impl.IQStateOptionalTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools.rightSpecificVariables;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;
import static it.unibz.inf.ontop.iq.impl.NaryIQTreeTools.replaceChild;


public class InnerJoinNormalizerImpl implements InnerJoinNormalizer {

    private static final int MAX_ITERATIONS = 10000;
    private static final int BINDING_LIFT_ITERATIONS = 1000;

    private final JoinLikeChildBindingLifter bindingLifter;
    private final IntermediateQueryFactory iqFactory;
    private final ConstructionSubstitutionNormalizer substitutionNormalizer;
    private final ConditionSimplifier conditionSimplifier;
    private final TermFactory termFactory;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;
    private final IQTreeTools iqTreeTools;

    @Inject
    private InnerJoinNormalizerImpl(JoinLikeChildBindingLifter bindingLifter, IntermediateQueryFactory iqFactory,
                                    ConstructionSubstitutionNormalizer substitutionNormalizer,
                                    ConditionSimplifier conditionSimplifier, TermFactory termFactory,
                                    JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                    IQTreeTools iqTreeTools) {
        this.bindingLifter = bindingLifter;
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
        Context context = new Context(innerJoinNode, children, variableGenerator, treeCache);
        return context.normalize();
    }

    private class Context extends NormalizationContext {

        private final InnerJoinNode innerJoinNode;
        private final ImmutableList<IQTree> initialChildren;
        private final IQTreeCache treeCache;

        private final ImmutableSet<Variable> projectedVariables;

        private Context(InnerJoinNode innerJoinNode, ImmutableList<IQTree> initialChildren, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator);
            this.innerJoinNode = innerJoinNode;
            this.initialChildren = initialChildren;
            this.treeCache = treeCache;
            this.projectedVariables = NaryIQTreeTools.projectedVariables(initialChildren);
        }

        IQTree normalize() {
            return IQStateOptionalTransformer.reachFixedPoint(
                            new State(UnaryOperatorSequence.of(), innerJoinNode.getOptionalFilterCondition(), initialChildren),
                            s -> s.liftBindingsAndDistincts()
                                    .liftFilterInnerJoinProjectingConstruction(),
                            MAX_ITERATIONS)
                    .asIQTree();
        }

        /**
         * A sequence of ConstructionNode and DistinctNode, followed by an InnerJoinNode,
         * followed by its children trees
         */

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private class State extends NormalizationState<UnaryOperatorNode> {
            private final Optional<ImmutableExpression> joiningCondition;
            private final ImmutableList<IQTree> children;

            private State(UnaryOperatorSequence<UnaryOperatorNode> ancestors, Optional<ImmutableExpression> joiningCondition, ImmutableList<IQTree> children) {
                super(ancestors);
                this.joiningCondition = joiningCondition;
                this.children = children;
            }

            State update(Optional<ImmutableExpression> newCondition, ImmutableList<IQTree> newChildren) {
                return new State(getAncestors(), newCondition, newChildren);
            }

            State update(Optional<? extends UnaryOperatorNode> newParent, Optional<ImmutableExpression> newCondition, ImmutableList<IQTree> newChildren) {
                return new State(getAncestors().append(newParent), newCondition, newChildren);
            }

            /**
             * No child is interpreted as EMPTY
             */
            State declareAsEmpty() {
                EmptyNode emptyChild = iqFactory.createEmptyNode(projectedVariables);
                return new State(UnaryOperatorSequence.of(), Optional.empty(), ImmutableList.of(emptyChild));
            }

            @Override
            public boolean equals(Object o) {
                if (o == this) return true;
                if (o instanceof State) {
                    State that = (State) o;
                    return joiningCondition.equals(that.joiningCondition)
                            && children.equals(that.children)
                            && getAncestors().equals(that.getAncestors());
                }
                return false;
            }


            /**
             * Lifts bindings but children still project away irrelevant variables
             * (needed for limiting as much as possible the number of variables on which DISTINCT is applied)
             * <p>
             * NB: Note that this number is not guaranteed to be minimal. However, it is guaranteed to be sound.
             */
            State liftBindingsAndDistincts() {
                return IQStateOptionalTransformer.reachFinalState(
                        this, s -> s.propagateDownCondition().liftBindings(), State::liftDistinct);
            }

            State liftBindings() {
                return IQStateOptionalTransformer.reachFinalState(
                        this, State::normalizeChildren, State::liftOneChildBinding);
            }

            State normalizeChildren() {
                ImmutableList<IQTree> liftedChildren = children.stream()
                        .map(c -> c.normalizeForOptimization(variableGenerator))
                        .filter(c -> !(c.getRootNode() instanceof TrueNode))
                        .collect(ImmutableCollectors.toList());

                if (liftedChildren.stream()
                        .anyMatch(IQTree::isDeclaredAsEmpty))
                    return declareAsEmpty();

                return update(joiningCondition, liftedChildren);
            }

            Optional<State> liftOneChildBinding() {
                return IntStream.range(0, children.size())
                        .mapToObj(i -> children.get(i).acceptVisitor(new IQStateOptionalTransformer<State>() {
                            @Override
                            public Optional<State> transformConstruction(UnaryIQTree tree, ConstructionNode constructionNode, IQTree grandChild) {
                                return liftBinding(i, constructionNode, grandChild);
                            }
                        }))
                        .flatMap(Optional::stream)
                        .findFirst();
            }

            Optional<State> liftBinding(int position, ConstructionNode constructionNode, IQTree grandChild) {
                if (constructionNode.getSubstitution().isEmpty())
                    return Optional.empty();

                try {
                    IQTree selectedGrandChildWithLimitedProjection =
                            iqTreeTools.unaryIQTreeBuilder(constructionNode.getChildVariables())
                                    .build(grandChild);

                    var provisionalNewChildren = replaceChild(children, position, selectedGrandChildWithLimitedProjection);

                    var bindingLift = bindingLifter.liftRegularChildBinding(
                            constructionNode,
                            position,
                            children,
                            ImmutableSet.of(),
                            joiningCondition,
                            variableGenerator,
                            variableNullabilityTools.getChildrenVariableNullability(provisionalNewChildren));

                    var projectedVariables = NaryIQTreeTools.projectedVariables(children);

                    ConstructionSubstitutionNormalization normalization = substitutionNormalizer
                            .normalizeSubstitution(bindingLift.getAscendingSubstitution(), projectedVariables);

                    Optional<ImmutableExpression> newCondition = bindingLift.getCondition()
                            .map(normalization::updateExpression);

                    Substitution<? extends VariableOrGroundTerm> descendingSubstitution = bindingLift.getDescendingSubstitution();
                    ImmutableList<IQTree> newChildren = provisionalNewChildren.stream()
                            .map(c -> c.applyDescendingSubstitution(descendingSubstitution, newCondition, variableGenerator))
                            .map(c -> normalization.updateChild(c, variableGenerator))
                            .collect(ImmutableCollectors.toList());

                    Optional<ConstructionNode> newParent =
                            iqTreeTools.createOptionalConstructionNode(() -> projectedVariables, normalization.getNormalizedSubstitution());

                    return Optional.of(update(newParent, newCondition, newChildren));
                }
                catch (UnsatisfiableConditionException e) {
                    return Optional.of(declareAsEmpty());
                }
            }


            @Override
            protected IQTree asIQTree() {

                IQTree joinLevelTree = createJoinOrFilterOrEmptyOrLiftLeft(treeCache.declareAsNormalizedForOptimizationWithEffect());
                if (joinLevelTree.isDeclaredAsEmpty())
                    return joinLevelTree;

                IQTree nonNormalizedTree = iqTreeTools.unaryIQTreeBuilder(projectedVariables)
                        .append(getAncestors())
                        .build(joinLevelTree);

                // Normalizes the ancestors (recursive)
                return nonNormalizedTree.normalizeForOptimization(variableGenerator);
            }


            private IQTree createJoinOrFilterOrEmptyOrLiftLeft(IQTreeCache normalizedTreeCache) {
                switch (children.size()) {
                    case 0:
                        return iqFactory.createTrueNode();
                    case 1:
                        return iqTreeTools.unaryIQTreeBuilder()
                                .append(iqTreeTools.createOptionalFilterNode(joiningCondition))
                                .build(children.get(0));
                    default:
                        return liftOneLeftJoin()
                                .orElseGet(() -> iqFactory.createNaryIQTree(
                                        iqFactory.createInnerJoinNode(joiningCondition),
                                        children, normalizedTreeCache));
                }
            }

            /**
             * Puts the LJ above the inner join if possible
             */
            Optional<IQTree> liftOneLeftJoin() {
                return IntStream.range(0, children.size())
                        .mapToObj(i -> children.get(i).acceptVisitor(new IQStateOptionalTransformer<IQTree>() {
                            @Override
                            public Optional<IQTree> transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode node, IQTree leftChild, IQTree rightChild) {
                                return liftLeftJoin(i, node, leftChild, rightChild);
                            }
                        }))
                        .flatMap(Optional::stream)
                        .findFirst();
            }

            Optional<IQTree> liftLeftJoin(int index, LeftJoinNode node, IQTree leftChild, IQTree rightChild) {

                // For safety (although conflicts are unlikely to appear)
                Set<Variable> rightSpecificVariables = rightSpecificVariables(leftChild, rightChild);

                if (!IntStream.range(0, children.size())
                        .filter(i -> i != index)
                        .mapToObj(children::get)
                        .map(IQTree::getVariables)
                        .allMatch(v -> Sets.intersection(v, rightSpecificVariables).isEmpty()))
                    return Optional.empty();

                NaryIQTree newJoinOnLeft = iqTreeTools.createInnerJoinTree(
                        Stream.concat(Stream.of(leftChild),
                                        IntStream.range(0, children.size())
                                                .filter(i -> i != index)
                                                .mapToObj(children::get))
                                .collect(ImmutableCollectors.toList()));

                return Optional.of(iqTreeTools.unaryIQTreeBuilder()
                        .append(iqTreeTools.createOptionalFilterNode(joiningCondition))
                        .build(iqFactory.createBinaryNonCommutativeIQTree(node, newJoinOnLeft, rightChild)));
            }

            /**
             * TODO: collect the constraint
             */
            State propagateDownCondition() {
                // TODO: consider that case as well
                if (joiningCondition.isEmpty())
                    return this;

                try {
                    // cache in the state?
                    var childrenVariableNullability = variableNullabilityTools.getChildrenVariableNullability(children);

                    var simplifiedJoinCondition = conditionSimplifier.simplifyCondition(
                            joiningCondition, ImmutableSet.of(), children, childrenVariableNullability);

                    var extendedDownConstraint = conditionSimplifier.extendAndSimplifyDownConstraint(
                            new DownPropagation(projectedVariables), simplifiedJoinCondition, childrenVariableNullability);

                    ImmutableList<IQTree> newChildren = extendedDownConstraint.propagate(
                            children, variableGenerator);

                    Optional<ImmutableExpression> newJoiningCondition = simplifiedJoinCondition.getOptionalExpression();

                    Optional<ConstructionNode> newParent = iqTreeTools.createOptionalConstructionNode(
                            () -> NaryIQTreeTools.projectedVariables(children),
                            simplifiedJoinCondition.getSubstitution());

                    return update(newParent, newJoiningCondition, newChildren);
                }
                catch (UnsatisfiableConditionException e) {
                    return declareAsEmpty();
                }
            }

            Optional<State> liftDistinct() {
                return children.stream()
                        .map(c -> c.acceptVisitor(new IQStateOptionalTransformer<State>() {
                            @Override
                            public Optional<State> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree distinctChild) {
                                if (isDistinct()) {
                                    ImmutableList<IQTree> newChildren = children.stream()
                                            .map(IQTree::removeDistincts)
                                            .collect(ImmutableCollectors.toList());

                                    return Optional.of(update(Optional.of(node), joiningCondition, newChildren));
                                }
                                return Optional.empty();
                            }
                        }))
                        .flatMap(Optional::stream)
                        .findFirst();
            }

            boolean isDistinct() {
                if (children.stream().allMatch(IQTree::isDistinct))
                    return true;

                IQTree tree = iqTreeTools.createInnerJoinTree(joiningCondition, children);
                return tree.isDistinct();
            }


            State liftFilterInnerJoinProjectingConstruction() {
                var childLifts = children.stream()
                        .map(this::getChildLift)
                        .collect(ImmutableCollectors.toList());

                var newJoiningCondition = termFactory.getConjunction(
                        joiningCondition,
                        childLifts.stream().flatMap(ChildLift::expressionStream));

                var newChildren = childLifts.stream()
                        .flatMap(ChildLift::childrenStream)
                        .collect(ImmutableCollectors.toList());

                if (children.equals(newChildren))
                    return this;

                return update(
                        Optional.of(iqFactory.createConstructionNode(
                            NaryIQTreeTools.projectedVariables(children))),
                        newJoiningCondition,
                        newChildren);
            }

            ChildLift getChildLift(IQTree tree) {
                return tree.acceptVisitor(new IQStateDefaultTransformer<>() {
                    @Override
                    protected ChildLift done() {
                        return new ChildLift(Stream.empty(), Stream.of(tree));
                    }

                    @Override
                    public ChildLift transformFilter(UnaryIQTree tree, FilterNode filterNode, IQTree child) {
                        return new ChildLift(Stream.of(filterNode.getFilterCondition()), Stream.of(child));
                    }

                    @Override
                    public ChildLift transformConstruction(UnaryIQTree tree, ConstructionNode constructionNode, IQTree child) {
                        if (constructionNode.getSubstitution().isEmpty())
                            // TODO: check whether projected away variables need to be renamed
                            //  (in case they occur in other children)
                            return new ChildLift(Stream.of(), Stream.of(child));

                        return done();
                    }

                    @Override
                    public ChildLift transformInnerJoin(NaryIQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> children) {
                        return new ChildLift(joinNode.getOptionalFilterCondition().stream(), children.stream());
                    }
                });
            }
        }
    }

    private static final class ChildLift {
        private final Stream<ImmutableExpression> expressionStream;
        private final Stream<IQTree> childrenStream;

        ChildLift(Stream<ImmutableExpression> expressionStream, Stream<IQTree> childrenStream) {
            this.expressionStream = expressionStream;
            this.childrenStream = childrenStream;
        }

        Stream<ImmutableExpression> expressionStream() {
            return expressionStream;
        }

        Stream<IQTree> childrenStream() {
            return childrenStream;
        }
    }
}
