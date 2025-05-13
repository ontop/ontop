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

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;


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

    private class Context {

        private final InnerJoinNode innerJoinNode;
        private final ImmutableList<IQTree> initialChildren;
        private final VariableGenerator variableGenerator;
        private final IQTreeCache treeCache;

        private final ImmutableSet<Variable> projectedVariables;

        private Context(InnerJoinNode innerJoinNode, ImmutableList<IQTree> initialChildren, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            this.innerJoinNode = innerJoinNode;
            this.initialChildren = initialChildren;
            this.variableGenerator = variableGenerator;
            this.treeCache = treeCache;
            this.projectedVariables = iqTreeTools.getChildrenVariables(initialChildren);
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
        private class State {
            private final UnaryOperatorSequence<UnaryOperatorNode> ancestors;
            private final Optional<ImmutableExpression> joiningCondition;
            private final ImmutableList<IQTree> children;

            private State(UnaryOperatorSequence<UnaryOperatorNode> ancestors, Optional<ImmutableExpression> joiningCondition, ImmutableList<IQTree> children) {
                this.ancestors = ancestors;
                this.joiningCondition = joiningCondition;
                this.children = children;
            }

            State update(Optional<ImmutableExpression> newCondition, ImmutableList<IQTree> newChildren) {
                return new State(ancestors, newCondition, newChildren);
            }

            State update(Optional<? extends UnaryOperatorNode> newParent, Optional<ImmutableExpression> newCondition, ImmutableList<IQTree> newChildren) {
                return new State(ancestors.append(newParent), newCondition, newChildren);
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
                            && ancestors.equals(that.ancestors);
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
                            iqTreeTools.createConstructionNodeTreeIfNontrivial(grandChild, constructionNode.getChildVariables());

                    var provisionalNewChildren = IntStream.range(0, children.size())
                            .mapToObj(i -> i == position ? selectedGrandChildWithLimitedProjection : children.get(i))
                            .collect(ImmutableCollectors.toList());

                    var bindingLift = bindingLifter.liftRegularChildBinding(
                            constructionNode,
                            position,
                            children,
                            ImmutableSet.of(),
                            joiningCondition,
                            variableGenerator,
                            variableNullabilityTools.getChildrenVariableNullability(provisionalNewChildren));

                    ConstructionSubstitutionNormalization normalization = substitutionNormalizer
                            .normalizeSubstitution(bindingLift.getAscendingSubstitution(), iqTreeTools.getChildrenVariables(children));

                    Optional<ImmutableExpression> newCondition = bindingLift.getCondition()
                            .map(normalization::updateExpression);

                    Substitution<? extends VariableOrGroundTerm> descendingSubstitution = bindingLift.getDescendingSubstitution();
                    ImmutableList<IQTree> newChildren = provisionalNewChildren.stream()
                            .map(c -> c.applyDescendingSubstitution(descendingSubstitution, newCondition, variableGenerator))
                            .map(c -> normalization.updateChild(c, variableGenerator))
                            .collect(ImmutableCollectors.toList());

                    Optional<ConstructionNode> newParent = normalization.generateTopConstructionNode();

                    return Optional.of(update(newParent, newCondition, newChildren));
                }
                catch (UnsatisfiableConditionException e) {
                    return Optional.of(declareAsEmpty());
                }
            }


            IQTree asIQTree() {

                IQTree joinLevelTree = createJoinOrFilterOrEmptyOrLiftLeft(treeCache.declareAsNormalizedForOptimizationWithEffect());

                if (joinLevelTree.isDeclaredAsEmpty())
                    return joinLevelTree;

                IQTree ancestorTree = iqTreeTools.createAncestorsUnaryIQTree(ancestors, joinLevelTree);

                IQTree nonNormalizedTree = iqTreeTools.createConstructionNodeTreeIfNontrivial(ancestorTree, projectedVariables);

                // Normalizes the ancestors (recursive)
                return nonNormalizedTree.normalizeForOptimization(variableGenerator);
            }


            IQTree createJoinOrFilterOrEmptyOrLiftLeft(IQTreeCache normalizedTreeCache) {
                switch (children.size()) {
                    case 0:
                        return iqFactory.createTrueNode();
                    case 1:
                        return iqTreeTools.createFilterTree(joiningCondition, children.get(0));
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
                Set<Variable> rightSpecificVariables = Sets.difference(rightChild.getVariables(), leftChild.getVariables());

                if (!IntStream.range(0, children.size())
                        .filter(i -> i != index)
                        .mapToObj(children::get)
                        .map(IQTree::getVariables)
                        .allMatch(v -> Sets.intersection(v, rightSpecificVariables).isEmpty()))
                    return Optional.empty();

                NaryIQTree newJoinOnLeft = iqFactory.createNaryIQTree(
                        iqFactory.createInnerJoinNode(),
                        Stream.concat(Stream.of(leftChild),
                                        IntStream.range(0, children.size())
                                                .filter(i -> i != index)
                                                .mapToObj(children::get))
                                .collect(ImmutableCollectors.toList()));

                return Optional.of(iqTreeTools.createFilterTree(
                        joiningCondition,
                        iqFactory.createBinaryNonCommutativeIQTree(node, newJoinOnLeft, rightChild)));
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

                    ConditionSimplifier.ExpressionAndSubstitution conditionSimplificationResults = conditionSimplifier.simplifyCondition(
                            joiningCondition.get(), children, childrenVariableNullability);

                    Optional<ImmutableExpression> newJoiningCondition = conditionSimplificationResults.getOptionalExpression();
                    // TODO: build a proper constraint (more than just the joining condition)

                    ImmutableList<IQTree> newChildren = iqTreeTools.applyDescendingSubstitution(
                            children, conditionSimplificationResults.getSubstitution(), newJoiningCondition, variableGenerator);

                    Optional<ConstructionNode> newParent = Optional.of(conditionSimplificationResults.getSubstitution())
                            .filter(s -> !s.isEmpty())
                            .map(s -> iqFactory.createConstructionNode(iqTreeTools.getChildrenVariables(children), s));

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

                IQTree tree = iqFactory.createNaryIQTree(
                        iqFactory.createInnerJoinNode(joiningCondition),
                        children);
                return tree.isDistinct();
            }


            State liftFilterInnerJoinProjectingConstruction() {
                var childLifts = children.stream()
                        .map(this::getChildLift)
                        .collect(ImmutableCollectors.toList());

                var newJoiningCondition = termFactory.getConjunction(
                        joiningCondition,
                        childLifts.stream().flatMap(ChildLift::getExpressionStream));

                var newChildren = childLifts.stream()
                        .flatMap(ChildLift::getChildrenStream)
                        .collect(ImmutableCollectors.toList());

                var optionalNewParent = Optional.of(children)
                        .filter(c -> !newChildren.equals(c))
                        .map(iqTreeTools::getChildrenVariables)
                        .map(iqFactory::createConstructionNode);

                return update(optionalNewParent, newJoiningCondition, newChildren);
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

    private static class ChildLift {
        private final Stream<ImmutableExpression> expressionStream;
        private final Stream<IQTree> childrenStream;

        private ChildLift(Stream<ImmutableExpression> expressionStream, Stream<IQTree> childrenStream) {
            this.expressionStream = expressionStream;
            this.childrenStream = childrenStream;
        }

        public Stream<ImmutableExpression> getExpressionStream() {
            return expressionStream;
        }

        public Stream<IQTree> getChildrenStream() {
            return childrenStream;
        }
    }
}
