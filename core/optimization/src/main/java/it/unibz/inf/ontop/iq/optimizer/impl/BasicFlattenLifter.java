package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.FlattenLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class BasicFlattenLifter implements FlattenLifter {

    private final IntermediateQueryFactory iqFactory;


    @Inject
    private BasicFlattenLifter(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
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
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            IQTree updatedChild = child.acceptTransformer(this);
            FlattenSubtree flattenSubtree = flattenSubtreeOf(updatedChild);
            if (flattenSubtree.flattenNodes.isEmpty()) {
                return iqFactory.createUnaryIQTree(rootNode, updatedChild);
            }

            FlattenSubtree subtree = rootNode.getFilterCondition().flattenAND()
                    .collect(simpleAccumulatorOf(
                            flattenSubtree,
                            (t, c) -> {
                                FlattenSplit split = t.split(c.getVariables());
                                return split.insertAboveNonLiftable(iqFactory.createFilterNode(c));
                            }));

            return subtree.asIQTree();
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode cn, IQTree child) {
            IQTree updatedChild = child.acceptTransformer(this);
            if (tree.getRootNode().equals(cn)) {
                return iqFactory.createUnaryIQTree(cn, updatedChild);
            }

            FlattenSplit split = splitOf(updatedChild, ImmutableSet.of());
            FlattenSubtree subtree = split.insertAboveNonLiftable(
                    split.liftableFlatten.stream()
                            .collect(simpleAccumulatorOf(cn, this::updateConstructionNode)));

            return subtree.asIQTree();
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
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode join, ImmutableList<IQTree> initialChildren) {
            ImmutableList<IQTree> children = initialChildren.stream()
                    .map(c -> c.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            ImmutableSet<Variable> blockingVars = getImplicitJoinCondition(children);

            ImmutableList<FlattenSplit> splits = children.stream()
                    .map(c -> splitOf(c, blockingVars))
                    .collect(ImmutableCollectors.toList());

            FlattenSubtree subtree = new FlattenSubtree(
                    splits.stream()
                            .flatMap(fs -> fs.liftableFlatten.stream())
                            .collect(ImmutableCollectors.toList()),
                    iqFactory.createNaryIQTree(join, splits.stream()
                            .map(FlattenSplit::nonLiftableSubtree)
                            .collect(ImmutableCollectors.toList())));

            return subtree.asIQTree();
        }

        /**
         * For now the implementation is identical to the one of inner joins, with the exception that all variables involved in the LJ condition are blocking.
         * TODO: identify additional cases where flatten nodes could be lifted, by creating a filter
         * (note that this only possible if extra integrity constraints hold, or in the presence of a distinct)
         */
        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree initialLeftChild, IQTree initialRightChild) {
            IQTree leftChild = initialLeftChild.acceptTransformer(this);
            IQTree rightChild = initialRightChild.acceptTransformer(this);

            // all variables involved in the joining condition are blocking
            ImmutableSet<Variable> blockingVars = Sets.union(
                    getImplicitJoinCondition(ImmutableList.of(leftChild, rightChild)),
                    rootNode.getLocallyRequiredVariables()
            ).immutableCopy();

            FlattenSplit leftSplit = splitOf(leftChild, blockingVars);
            FlattenSplit rightSplit = splitOf(rightChild, blockingVars);

            FlattenSubtree subtree = new FlattenSubtree(
                    Stream.of(leftSplit, rightSplit)
                            .flatMap(s -> s.liftableFlatten.stream())
                            .collect(ImmutableCollectors.toList()),
                    iqFactory.createBinaryNonCommutativeIQTree(
                            rootNode,
                            leftSplit.nonLiftableSubtree(),
                            rightSplit.nonLiftableSubtree()));

            return subtree.asIQTree();
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

    private class FlattenSubtree {
        private final ImmutableList<FlattenNode> flattenNodes;
        private final IQTree child;

        FlattenSubtree(ImmutableList<FlattenNode> flattenNodes, IQTree child) {
            this.flattenNodes = flattenNodes;
            this.child = child;
        }

        FlattenSplit split(ImmutableSet<Variable> blockingVars) {
            return flattenNodes.stream()
                    .collect(simpleAccumulatorOf(
                            new FlattenSplit(child, ImmutableList.of(), ImmutableList.of(), blockingVars),
                            FlattenSplit::addFlattenNode));
        }

        IQTree asIQTree() {
            return buildUnaryTree(flattenNodes, child);
        }
    }

    private FlattenSubtree flattenSubtreeOf(IQTree tree) {
        ImmutableList<FlattenNode> flattenNodes = Stream.iterate(tree,
                        t -> t.getRootNode() instanceof FlattenNode,
                        t -> ((UnaryIQTree) t).getChild())
                .map(t -> (FlattenNode) t.getRootNode())
                .collect(ImmutableCollectors.toList());

        IQTree child = flattenNodes.stream()
                .collect(simpleAccumulatorOf(
                        tree,
                        (t, fn) -> {
                            if (t.getRootNode() != fn)
                                throw new MinorOntopInternalBugException("Node " + fn + " is expected top be the root of " + t);
                            return ((UnaryIQTree) t).getChild();
                        }));

        return new FlattenSubtree(flattenNodes, child);
    }


    private class FlattenSplit {
        private final IQTree child;
        private final ImmutableList<FlattenNode> liftableFlatten;
        private final ImmutableList<FlattenNode> nonLiftableFlatten;
        private final ImmutableSet<Variable> blockingVars;

        FlattenSplit(IQTree child, ImmutableList<FlattenNode> liftableFlatten, ImmutableList<FlattenNode> nonLiftableFlatten, ImmutableSet<Variable> blockingVars) {
            this.child = child;
            this.liftableFlatten = liftableFlatten;
            this.nonLiftableFlatten = nonLiftableFlatten;
            this.blockingVars = blockingVars;
        }

        FlattenSplit addFlattenNode(FlattenNode fn) {
            return Sets.intersection(fn.getLocallyDefinedVariables(), blockingVars).isEmpty()
                    ? new FlattenSplit(
                        child,
                        Stream.concat(liftableFlatten.stream(),Stream.of(fn)).collect(ImmutableCollectors.toList()),
                        nonLiftableFlatten,
                        blockingVars)
                    : new FlattenSplit(
                        child,
                        liftableFlatten,
                        Stream.concat(nonLiftableFlatten.stream(), Stream.of(fn)).collect(ImmutableCollectors.toList()),
                        Stream.concat(blockingVars.stream(), Stream.of(fn.getFlattenedVariable())).collect(ImmutableCollectors.toSet()));
        }

        IQTree nonLiftableSubtree() {
            return buildUnaryTree(nonLiftableFlatten, child);
        }

        FlattenSubtree insertAboveNonLiftable(UnaryOperatorNode node) {
            return new FlattenSubtree(
                    liftableFlatten,
                    iqFactory.createUnaryIQTree(node, nonLiftableSubtree()));
        }
    }

    private FlattenSplit splitOf(IQTree tree, ImmutableSet<Variable> blockingVars) {
        FlattenSubtree flattenSubtree = flattenSubtreeOf(tree);
        return flattenSubtree.split(blockingVars);
    }

    private IQTree buildUnaryTree(List<FlattenNode> list, IQTree subtree) {
        // TODO: use reversed() in Java 21
        Deque<FlattenNode> deque = new ArrayDeque<>(list);
        return Streams.stream(deque.descendingIterator())
                .collect(simpleAccumulatorOf(subtree,
                        (t, n) -> iqFactory.createUnaryIQTree(n, t)));
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
