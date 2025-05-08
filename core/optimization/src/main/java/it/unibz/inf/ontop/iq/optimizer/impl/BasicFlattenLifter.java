package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.FlattenLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.BiFunction;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.NestedUnaryIQTreeDecomposition;

public class BasicFlattenLifter implements FlattenLifter {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private BasicFlattenLifter(IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQ optimize(IQ query) {
        TreeTransformer treeTransformer = new TreeTransformer();
        return iqFactory.createIQ(
                query.getProjectionAtom(),
                query.getTree().acceptTransformer(treeTransformer));
    }

    private class TreeTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        TreeTransformer() {
            super(BasicFlattenLifter.this.iqFactory);
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            IQTree updatedChild = transformChild(child);
            NestedUnaryIQTreeDecomposition<FlattenNode> flattenSubtree = flattenSubtreeOf(updatedChild);
            if (flattenSubtree.getSequence().isEmpty()) {
                return iqFactory.createUnaryIQTree(rootNode, updatedChild);
            }

            NestedUnaryIQTreeDecomposition<FlattenNode> subtree = rootNode.getFilterCondition().flattenAND()
                    .collect(simpleAccumulatorOf(
                            flattenSubtree,
                            (t, c) -> {
                                ImmutableSet<Variable> blockingVars = c.getVariables();
                                FlattenSplit split = split(t, blockingVars);
                                return split.insertAboveNonLiftable(iqFactory.createFilterNode(c));
                            }));

            return asIQTree(subtree);
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode cn, IQTree child) {
            IQTree updatedChild = transformChild(child);
            if (tree.getRootNode().equals(cn)) {
                return iqFactory.createUnaryIQTree(cn, updatedChild);
            }

            FlattenSplit split = splitOf(updatedChild, ImmutableSet.of());
            NestedUnaryIQTreeDecomposition<FlattenNode> subtree = split.insertAboveNonLiftable(
                    split.liftableFlatten.stream()
                            .collect(simpleAccumulatorOf(cn, this::updateConstructionNode)));

            return asIQTree(subtree);
        }

        private ConstructionNode updateConstructionNode(ConstructionNode cn, FlattenNode fn) {
            ImmutableSet<Variable> projectedVars = Sets.union(
                            Sets.difference(cn.getVariables(), fn.getLocallyDefinedVariables()),
                            ImmutableSet.of(fn.getFlattenedVariable()))
                    .immutableCopy();

            return iqFactory.createConstructionNode(projectedVars, cn.getSubstitution());
        }

        /**
         * Assumption: the join carries no (explicit) joining condition
         */
        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode join, ImmutableList<IQTree> initialChildren) {
            ImmutableList<IQTree> children = transformChildren(initialChildren);

            ImmutableSet<Variable> blockingVars = getImplicitJoinCondition(children);

            ImmutableList<FlattenSplit> splits = children.stream()
                    .map(c -> splitOf(c, blockingVars))
                    .collect(ImmutableCollectors.toList());

            NestedUnaryIQTreeDecomposition<FlattenNode> subtree = NestedUnaryIQTreeDecomposition.of(
                    UnaryOperatorSequence.of(splits.stream()
                            .map(fs -> fs.liftableFlatten)),
                    iqFactory.createNaryIQTree(join, splits.stream()
                            .map(FlattenSplit::nonLiftableSubtree)
                            .collect(ImmutableCollectors.toList())));

            return asIQTree(subtree);
        }

        /**
         * For now the implementation is identical to the one of inner joins, with the exception that all variables involved in the LJ condition are blocking.
         * TODO: identify additional cases where flatten nodes could be lifted, by creating a filter
         * (note that this only possible if extra integrity constraints hold, or in the presence of a distinct)
         */
        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree initialLeftChild, IQTree initialRightChild) {
            IQTree leftChild = transformChild(initialLeftChild);
            IQTree rightChild = transformChild(initialRightChild);

            // all variables involved in the joining condition are blocking
            ImmutableSet<Variable> blockingVars = Sets.union(
                    getImplicitJoinCondition(ImmutableList.of(leftChild, rightChild)),
                    rootNode.getLocallyRequiredVariables()
            ).immutableCopy();

            FlattenSplit leftSplit = splitOf(leftChild, blockingVars);
            FlattenSplit rightSplit = splitOf(rightChild, blockingVars);

            NestedUnaryIQTreeDecomposition<FlattenNode> subtree = NestedUnaryIQTreeDecomposition.of(
                    UnaryOperatorSequence.of(Stream.of(
                            leftSplit.liftableFlatten,
                            rightSplit.liftableFlatten)),
                    iqFactory.createBinaryNonCommutativeIQTree(
                            rootNode,
                            leftSplit.nonLiftableSubtree(),
                            rightSplit.nonLiftableSubtree()));

            return asIQTree(subtree);
        }

        private ImmutableSet<Variable> getImplicitJoinCondition(ImmutableList<IQTree> children) {
            return children.stream()
                    .flatMap(n -> n.getVariables().stream())
                    .collect(ImmutableCollectors.toMultiset())
                    .entrySet().stream()
                    .filter(e -> e.getCount() > 1)
                    .map(Multiset.Entry::getElement)
                    .collect(ImmutableCollectors.toSet());
        }
    }

    private FlattenSplit split(NestedUnaryIQTreeDecomposition<FlattenNode> decomposition, ImmutableSet<Variable> blockingVars) {
        return decomposition.getSequence().stream()
                .collect(simpleAccumulatorOf(
                        new FlattenSplit(UnaryOperatorSequence.of(), NestedUnaryIQTreeDecomposition.of(UnaryOperatorSequence.of(), decomposition.getChild()), blockingVars),
                        FlattenSplit::addFlattenNode));
    }

    private IQTree asIQTree(NestedUnaryIQTreeDecomposition<FlattenNode> decomposition) {
        return iqTreeTools.createAncestorsUnaryIQTree(decomposition.getSequence(), decomposition.getChild());
    }

    private NestedUnaryIQTreeDecomposition<FlattenNode> flattenSubtreeOf(IQTree tree) {
        return NestedUnaryIQTreeDecomposition.of(tree, FlattenNode.class);
    }


    private class FlattenSplit {
        private final UnaryOperatorSequence<FlattenNode> liftableFlatten;
        private final NestedUnaryIQTreeDecomposition<FlattenNode> nonLiftableFlatten;
        private final ImmutableSet<Variable> blockingVars;

        FlattenSplit(UnaryOperatorSequence<FlattenNode> liftableFlatten, NestedUnaryIQTreeDecomposition<FlattenNode> nonLiftableFlatten, ImmutableSet<Variable> blockingVars) {
            this.liftableFlatten = liftableFlatten;
            this.nonLiftableFlatten = nonLiftableFlatten;
            this.blockingVars = blockingVars;
        }

        FlattenSplit addFlattenNode(FlattenNode fn) {
            return Sets.intersection(fn.getLocallyDefinedVariables(), blockingVars).isEmpty()
                    ? new FlattenSplit(
                        liftableFlatten.append(fn),
                        nonLiftableFlatten,
                        blockingVars)
                    : new FlattenSplit(
                        liftableFlatten,
                        nonLiftableFlatten.append(fn),
                        Stream.concat(blockingVars.stream(), Stream.of(fn.getFlattenedVariable())).collect(ImmutableCollectors.toSet()));
        }

        IQTree nonLiftableSubtree() {
            return iqTreeTools.createAncestorsUnaryIQTree(nonLiftableFlatten.getSequence(), nonLiftableFlatten.getChild());
        }

        NestedUnaryIQTreeDecomposition<FlattenNode> insertAboveNonLiftable(UnaryOperatorNode node) {
            return NestedUnaryIQTreeDecomposition.of(liftableFlatten,
                    iqFactory.createUnaryIQTree(node, nonLiftableSubtree()));
        }
    }

    private FlattenSplit splitOf(IQTree tree, ImmutableSet<Variable> blockingVars) {
        return split(flattenSubtreeOf(tree), blockingVars);
    }

    private static <T,A> Collector<T,?,A> simpleAccumulatorOf(A initial, BiFunction<A,T,A> function) {
        class Store {
            private A value;
        }

        return Collector.of(
                () -> { Store s = new Store(); s.value = initial; return s; },
                (s, t) -> { s.value = function.apply(s.value, t); },
                (a1, a2) -> { throw new UnsupportedOperationException("Should not be called"); },
                s -> s.value);
    }
}
