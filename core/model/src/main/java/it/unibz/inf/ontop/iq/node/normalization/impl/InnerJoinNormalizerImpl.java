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
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.NaryIQTreeTools.replaceChild;
import it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools.LeftJoinDecomposition;
import it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


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
        var initialInnerJoin = new InnerJoinSubTree(innerJoinNode.getOptionalFilterCondition(), children);
        Context context = new Context(initialInnerJoin.projectedVariables(), variableGenerator, treeCache);
        return context.normalize(initialInnerJoin);
    }

    /**
     * A sequence of ConstructionNode and DistinctNode, followed by an InnerJoinNode,
     * followed by its children trees
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class InnerJoinSubTree {
        private final Optional<ImmutableExpression> joiningCondition;
        private final ImmutableList<IQTree> children;

        private InnerJoinSubTree(Optional<ImmutableExpression> joiningCondition, ImmutableList<IQTree> children) {
            this.joiningCondition = joiningCondition;
            this.children = children;
        }

        Optional<ImmutableExpression> joiningCondition() {
            return joiningCondition;
        }

        ImmutableList<IQTree> children() {
            return children;
        }

        ImmutableSet<Variable> projectedVariables() {
            return NaryIQTreeTools.projectedVariables(children);
        }

        Stream<IQTree> childrenExcept(int position) {
            return IntStream.range(0, children.size())
                    .filter(i -> i != position)
                    .mapToObj(children::get);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof InnerJoinSubTree) {
                InnerJoinSubTree other = (InnerJoinSubTree)o;
                return joiningCondition.equals(other.joiningCondition)
                        && children.equals(other.children);
            }
            return false;
        }
    }

    private interface Lifter<P, S, D extends IQTreeTools.IQTreeDecomposition<?, ?>> {
        Optional<P> lift(S state, int i, D decomposition);

        static <P, S, D extends IQTreeTools.IQTreeDecomposition<?, ?>> Optional<P> liftFirst(S s, InnerJoinSubTree subTree, Lifter<P, S, D> lifter, Function<IQTree, D> decomposer) {
            var children = subTree.children();
            return IntStream.range(0, children.size())
                    .mapToObj(i ->
                            Optional.of(decomposer.apply(children.get(i)))
                                    .filter(d -> d.isPresent())
                                    .flatMap(d -> lifter.lift(s, i, d)))
                    .flatMap(Optional::stream)
                    .findFirst();
        }
    }


    private class Context extends NormalizationContext {

        private Context(ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(projectedVariables, variableGenerator, treeCache, InnerJoinNormalizerImpl.this.iqTreeTools);
        }

        IQTree normalize(InnerJoinSubTree initialInnerJoin) {
            var initial = State.initial(initialInnerJoin);
            var state = initial.reachFixedPoint(MAX_ITERATIONS,
                            this::liftBindingsAndDistincts,
                            this::liftFilterInnerJoinProjectingConstruction);
            return asIQTree(state);
        }


        /**
         * Lifts bindings but children still project away irrelevant variables
         * (needed for limiting as much as possible the number of variables on which DISTINCT is applied)
         * <p>
         * NB: Note that this number is not guaranteed to be minimal. However, it is guaranteed to be sound.
         */
        State<UnaryOperatorNode, InnerJoinSubTree> liftBindingsAndDistincts(State<UnaryOperatorNode, InnerJoinSubTree> state) {
            return state.reachFinal(
                    this::liftBindings,
                    s -> Lifter.liftFirst(s, s.getSubTree(), this::liftDistinct, c -> UnaryIQTreeDecomposition.of(c, DistinctNode.class)));
        }

        State<UnaryOperatorNode, InnerJoinSubTree> liftBindings(State<UnaryOperatorNode, InnerJoinSubTree> state) {
            return propagateDownCondition(state).reachFinal(
                    this::normalizeChildren,
                    s -> Lifter.liftFirst(s, s.getSubTree(), this::liftBinding, c -> UnaryIQTreeDecomposition.of(c, ConstructionNode.class)));
        }

        State<UnaryOperatorNode, InnerJoinSubTree> normalizeChildren(State<UnaryOperatorNode, InnerJoinSubTree> state) {
            InnerJoinSubTree subTree = state.getSubTree();
            ImmutableList<IQTree> liftedChildren = subTree.children().stream()
                    .map(this::normalizeSubTreeRecursively)
                    .filter(c -> !(c.getRootNode() instanceof TrueNode))
                    .collect(ImmutableCollectors.toList());

            if (liftedChildren.stream()
                    .anyMatch(IQTree::isDeclaredAsEmpty))
                return declareAsEmpty();

            return state.replace(new InnerJoinSubTree(subTree.joiningCondition(), liftedChildren));
        }

        /**
         * No child is interpreted as EMPTY
         */
        State<UnaryOperatorNode, InnerJoinSubTree> declareAsEmpty() {
            EmptyNode emptyChild = createEmptyNode();
            return State.initial(new InnerJoinSubTree(Optional.empty(), ImmutableList.of(emptyChild)));
        }


        Optional<State<UnaryOperatorNode, InnerJoinSubTree>> liftBinding(State<UnaryOperatorNode, InnerJoinSubTree> state, int position, UnaryIQTreeDecomposition<ConstructionNode> construction) {
            if (construction.getNode().getSubstitution().isEmpty())
                return Optional.empty();

            try {
                IQTree selectedGrandChildWithLimitedProjection =
                        iqTreeTools.unaryIQTreeBuilder(construction.getNode().getChildVariables())
                                .build(construction.getChild());

                InnerJoinSubTree subTree = state.getSubTree();
                var provisionalNewChildren = replaceChild(subTree.children(), position, selectedGrandChildWithLimitedProjection);

                var bindingLift = bindingLifter.liftRegularChildBinding(
                        construction.getNode(),
                        position,
                        subTree.children(),
                        ImmutableSet.of(),
                        subTree.joiningCondition(),
                        variableGenerator,
                        variableNullabilityTools.getChildrenVariableNullability(provisionalNewChildren));

                var projectedVariables = subTree.projectedVariables();

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

                return Optional.of(state.lift(newParent, new InnerJoinSubTree(newCondition, newChildren)));
            }
            catch (UnsatisfiableConditionException e) {
                return Optional.of(declareAsEmpty());
            }
        }



        /**
         * TODO: collect the constraint
         */
        State<UnaryOperatorNode, InnerJoinSubTree> propagateDownCondition(State<UnaryOperatorNode, InnerJoinSubTree> state) {
            InnerJoinSubTree subTree = state.getSubTree();
            // TODO: consider that case as well
            if (subTree.joiningCondition().isEmpty())
                return state;

            try {
                // cache in the state?
                var childrenVariableNullability = variableNullabilityTools.getChildrenVariableNullability(subTree.children());

                var simplifiedJoinCondition = conditionSimplifier.simplifyCondition(
                        subTree.joiningCondition(), ImmutableSet.of(), subTree.children(), childrenVariableNullability);

                var extendedDownConstraint = conditionSimplifier.extendAndSimplifyDownConstraint(
                        new DownPropagation(projectedVariables), simplifiedJoinCondition, childrenVariableNullability);

                ImmutableList<IQTree> newChildren = extendedDownConstraint.propagate(subTree.children(), variableGenerator);

                Optional<ImmutableExpression> newJoiningCondition = simplifiedJoinCondition.getOptionalExpression();

                Optional<ConstructionNode> newParent = iqTreeTools.createOptionalConstructionNode(
                        subTree::projectedVariables,
                        simplifiedJoinCondition.getSubstitution());

                return state.lift(newParent, new InnerJoinSubTree(newJoiningCondition, newChildren));
            }
            catch (UnsatisfiableConditionException e) {
                return declareAsEmpty();
            }
        }

        Optional<State<UnaryOperatorNode, InnerJoinSubTree>> liftDistinct(State<UnaryOperatorNode, InnerJoinSubTree> state, int position, UnaryIQTreeDecomposition<DistinctNode> distinct) {
            InnerJoinSubTree subTree = state.getSubTree();
            // this is just a shortcut - the check is also done below by isDistinct
            if (subTree.children().stream().allMatch(IQTree::isDistinct)
                    || iqTreeTools.createInnerJoinTree(subTree.joiningCondition(), subTree.children()).isDistinct()) {
                return Optional.of(state.lift(distinct.getNode(),
                        new InnerJoinSubTree(
                                subTree.joiningCondition(),
                                NaryIQTreeTools.transformChildren(subTree.children(), IQTree::removeDistincts))));
            }
            return Optional.empty();
        }

        protected IQTree asIQTree(State<UnaryOperatorNode, InnerJoinSubTree> state) {

            IQTree joinLevelTree = createJoinOrFilterOrEmptyOrLiftLeft(state.getSubTree());
            if (joinLevelTree.isDeclaredAsEmpty())
                return joinLevelTree;

            // normalize ancestors recursively
            return normalizeSubTreeRecursively(
                    iqTreeTools.unaryIQTreeBuilder(projectedVariables)
                            .append(state.getAncestors())
                            .build(joinLevelTree));
        }


        private IQTree createJoinOrFilterOrEmptyOrLiftLeft(InnerJoinSubTree subTree) {
            switch (subTree.children().size()) {
                case 0:
                    return iqFactory.createTrueNode();
                case 1:
                    return iqTreeTools.unaryIQTreeBuilder()
                            .append(iqTreeTools.createOptionalFilterNode(subTree.joiningCondition()))
                            .build(subTree.children().get(0));
                default:
                    return Lifter.liftFirst(subTree, subTree, this::liftLeftJoin, LeftJoinDecomposition::of)
                            .orElseGet(() -> iqFactory.createNaryIQTree(
                                    iqFactory.createInnerJoinNode(subTree.joiningCondition()),
                                    subTree.children(),
                                    getNormalizedTreeCache(true)));
            }
        }

        /**
         * Puts the LJ above the inner join if possible
         */
        Optional<IQTree> liftLeftJoin(InnerJoinSubTree subTree, int position, LeftJoinDecomposition leftJoin) {
            // For safety (although conflicts are unlikely to appear)
            Set<Variable> rightSpecificVariables = leftJoin.rightSpecificVariables();

            if (!subTree.childrenExcept(position)
                    .map(IQTree::getVariables)
                    .allMatch(v -> Sets.intersection(v, rightSpecificVariables).isEmpty()))
                return Optional.empty();

            NaryIQTree newJoinOnLeft = iqTreeTools.createInnerJoinTree(
                    Stream.concat(Stream.of(leftJoin.leftChild()),
                                    subTree.childrenExcept(position))
                            .collect(ImmutableCollectors.toList()));

            return Optional.of(iqTreeTools.unaryIQTreeBuilder()
                    .append(iqTreeTools.createOptionalFilterNode(subTree.joiningCondition()))
                    .build(iqFactory.createBinaryNonCommutativeIQTree(leftJoin.getNode(), newJoinOnLeft, leftJoin.rightChild())));
        }

        State<UnaryOperatorNode, InnerJoinSubTree> liftFilterInnerJoinProjectingConstruction(State<UnaryOperatorNode, InnerJoinSubTree> state) {
            InnerJoinSubTree subTree = state.getSubTree();
            var children = subTree.children();
            var childLifts = NaryIQTreeTools.transformChildren(children, this::getChildLift);

            var newChildren = childLifts.stream()
                    .map(InnerJoinSubTree::children)
                    .flatMap(ImmutableList::stream)
                    .collect(ImmutableCollectors.toList());

            if (children.equals(newChildren))
                return state;

            var newJoiningCondition = termFactory.getConjunction(
                    subTree.joiningCondition(),
                    childLifts.stream().map(InnerJoinSubTree::joiningCondition).flatMap(Optional::stream));

            return state.lift(
                    iqFactory.createConstructionNode(subTree.projectedVariables()),
                    new InnerJoinSubTree(newJoiningCondition, newChildren));
        }

        InnerJoinSubTree getChildLift(IQTree tree) {
            return tree.acceptVisitor(new IQStateDefaultTransformer<>() {
                @Override
                protected InnerJoinSubTree done() {
                    return new InnerJoinSubTree(Optional.empty(), ImmutableList.of(tree));
                }

                @Override
                public InnerJoinSubTree transformFilter(UnaryIQTree tree, FilterNode filterNode, IQTree child) {
                    return new InnerJoinSubTree(Optional.of(filterNode.getFilterCondition()), ImmutableList.of(child));
                }

                @Override
                public InnerJoinSubTree transformConstruction(UnaryIQTree tree, ConstructionNode constructionNode, IQTree child) {
                    if (constructionNode.getSubstitution().isEmpty())
                        // TODO: check whether projected away variables need to be renamed
                        //  (in case they occur in other children)
                        return new InnerJoinSubTree(Optional.empty(), ImmutableList.of(child));

                    return done();
                }

                @Override
                public InnerJoinSubTree transformInnerJoin(NaryIQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> children) {
                    return new InnerJoinSubTree(joinNode.getOptionalFilterCondition(), children);
                }
            });
        }
    }
}
