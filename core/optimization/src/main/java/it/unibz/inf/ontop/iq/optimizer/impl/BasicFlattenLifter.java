package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.FlattenLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
            child = child.acceptTransformer(this);

            ImmutableList<FlattenNode> flattenNodes = getRootFlattenNodes(child);
            if (flattenNodes.isEmpty()) {
                return iqFactory.createUnaryIQTree(rootNode, child);
            }
            child = discardRootFlattenNodes(child, flattenNodes);
            return liftAboveConjunct(
                    rootNode.getFilterCondition().flattenAND()
                            .collect(Collectors.toCollection(LinkedList::new)),
                    flattenNodes,
                    child);
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode cn, IQTree child) {
            child = child.acceptTransformer(this);
            if (tree.getRootNode().equals(cn)) {
                return iqFactory.createUnaryIQTree(cn, child);
            }

            ImmutableList<FlattenNode> flattenNodes = getRootFlattenNodes(child);

            child = discardRootFlattenNodes(child, flattenNodes);

            SplitFlattenSequence splitFlattenSequence = splitFlattenSequence(flattenNodes, ImmutableSet.of());

            return buildUnaryTree(
                    splitFlattenSequence.liftableFlatten,
                    iqFactory.createUnaryIQTree(
                            updateConstructionNode(cn, splitFlattenSequence.liftableFlatten),
                            buildUnaryTree(
                                    splitFlattenSequence.nonLiftableFlatten,
                                    child)));
        }

        /**
         * Assumption: the join carries no (explicit) joining condition
         */
        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode join, ImmutableList<IQTree> children0) {
            ImmutableList<IQTree> children = children0.stream()
                    .map(c -> c.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ImmutableList<FlattenNode>> flattenLists = children.stream()
                    .map(this::getRootFlattenNodes)
                    .collect(ImmutableCollectors.toList());

            ImmutableSet<Variable> blockingVars = getImplicitJoinCondition(children);

            ImmutableList<SplitFlattenSequence> flattenSequences = flattenLists.stream()
                    .map(l -> splitFlattenSequence(l, blockingVars))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<FlattenNode> liftedFlattenNodes = flattenSequences.stream()
                    .flatMap(fs -> fs.liftableFlatten.stream())
                    .collect(ImmutableCollectors.toList());

            ImmutableList<IQTree> childrenWithoutFlattenNodes = IntStream.range(0, children.size())
                    .mapToObj(i -> discardRootFlattenNodes(children.get(i), flattenLists.get(i)))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<IQTree> updatedChildren = IntStream.range(0, childrenWithoutFlattenNodes.size())
                    .mapToObj(i -> buildUnaryTree(flattenSequences.get(i).nonLiftableFlatten, childrenWithoutFlattenNodes.get(i)))
                    .collect(ImmutableCollectors.toList());

            return buildUnaryTree(
                    liftedFlattenNodes,
                    iqFactory.createNaryIQTree(
                            join,
                            updatedChildren));
        }

        /**
         * For now the implementation is identical to the one of inner joins, with the exception that all variables involved in the LJ condition are blocking.
         * TODO: identify additional cases where flatten nodes could be lifted, by creating a filter
         * (note that this only possible if extra integrity constraints hold, or in the presence of a distinct)
         */
        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            ImmutableList<IQTree> children0 = ImmutableList.of(leftChild, rightChild);

            ImmutableList<IQTree> children = children0.stream()
                    .map(c -> c.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ImmutableList<FlattenNode>> flattenLists = children.stream()
                    .map(this::getRootFlattenNodes)
                    .collect(ImmutableCollectors.toList());

            // all variables involved in the joining condition are blocking
            ImmutableSet<Variable> blockingVars = Sets.union(
                    getImplicitJoinCondition(children),
                    rootNode.getLocallyRequiredVariables()
            ).immutableCopy();

            Iterator<ImmutableList<FlattenNode>> it = flattenLists.iterator();

            ImmutableList<SplitFlattenSequence> flattenSequences = flattenLists.stream()
                    .map(l -> splitFlattenSequence(l, blockingVars))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<FlattenNode> liftedFlattenNodes = flattenSequences.stream()
                    .flatMap(fs -> fs.liftableFlatten.stream())
                    .collect(ImmutableCollectors.toList());

            ImmutableList<IQTree> childrenWithoutFlattenNodes = IntStream.range(0, children.size())
                    .mapToObj(i -> discardRootFlattenNodes(children.get(i), flattenLists.get(i)))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<IQTree> updatedChildren = IntStream.range(0, childrenWithoutFlattenNodes.size())
                    .mapToObj(i -> buildUnaryTree(flattenSequences.get(i).nonLiftableFlatten, childrenWithoutFlattenNodes.get(i)))
                    .collect(ImmutableCollectors.toList());

            return buildUnaryTree(
                    liftedFlattenNodes,
                    iqFactory.createBinaryNonCommutativeIQTree(
                            rootNode,
                            updatedChildren.get(0),
                            updatedChildren.get(1)));
        }

        private ConstructionNode updateConstructionNode(ConstructionNode cn, List<FlattenNode> list) {
            return list.stream()
                    .reduce(cn, this::updateConstructionNode, this::throwNoParallelStreamsException);
        }

        private ConstructionNode updateConstructionNode(ConstructionNode cn, FlattenNode fn) {
            ImmutableSet<Variable> projectedVars = Sets.union(
                            Sets.difference(cn.getVariables(), fn.getLocallyDefinedVariables()),
                            ImmutableSet.of(fn.getFlattenedVariable()))
                    .immutableCopy();

            return iqFactory.createConstructionNode(projectedVars, cn.getSubstitution());
        }

        private SplitFlattenSequence splitFlattenSequence(List<FlattenNode> flattenNodes, ImmutableSet<Variable> blockingVars) {
            List<FlattenNode> liftable = new ArrayList<>(), nonLiftable = new ArrayList<>();
            for (FlattenNode flattenNode : flattenNodes) {
                if (flattenNode.getLocallyDefinedVariables().stream()
                        .anyMatch(blockingVars::contains)) {
                    nonLiftable.add(flattenNode);
                    blockingVars = Sets.union(
                                    ImmutableSet.of(flattenNode.getFlattenedVariable()),
                                    blockingVars)
                            .immutableCopy();
                }
                else {
                    liftable.add(flattenNode);
                }
            }
            return new SplitFlattenSequence(ImmutableList.copyOf(liftable), ImmutableList.copyOf(nonLiftable));
        }

        private IQTree buildUnaryTree(List<FlattenNode> list, IQTree subtree) {
            // TODO: use reversed() in Java 21
            Deque<FlattenNode> deque = new ArrayDeque<>(list);
            return Streams.stream(deque.descendingIterator())
                    .reduce(subtree,
                            (t, n) -> iqFactory.createUnaryIQTree(n, t),
                            this::throwNoParallelStreamsException);
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

        private IQTree discardRootFlattenNodes(IQTree tree, ImmutableList<FlattenNode> flattenNodes) {
            return flattenNodes.stream()
                    .reduce(tree, this::discardRootFlattenNode, this::throwNoParallelStreamsException);
        }

        private IQTree discardRootFlattenNode(IQTree t, FlattenNode fn) {
            if (t.getRootNode() != fn)
                throw new FlattenLifterException("Node " + fn + " is expected top be the root of " + t);
            return ((UnaryIQTree)t).getChild();
        }

        private <T> T throwNoParallelStreamsException(T t1, T t2) {
            throw new MinorOntopInternalBugException("no parallel streams");
        }

        private ImmutableList<FlattenNode> getRootFlattenNodes(IQTree tree) {
            return Stream.iterate(tree,
                    t -> t.getRootNode() instanceof FlattenNode,
                    t -> ((UnaryIQTree)t).getChild())
                    .map(t -> (FlattenNode)t.getRootNode())
                    .collect(ImmutableCollectors.toList());
        }

        private IQTree liftAboveConjunct(LinkedList<ImmutableExpression> conjuncts, ImmutableList<FlattenNode> flattenNodes, IQTree child) {
            if (conjuncts.isEmpty()) {
                return buildUnaryTree(flattenNodes, child);
            }

            ImmutableExpression conjunct = conjuncts.removeFirst();
            SplitFlattenSequence split = splitFlattenSequence(flattenNodes, conjunct.getVariables());
            IQTree updatedChild = iqFactory.createUnaryIQTree(
                    iqFactory.createFilterNode(conjunct),
                    buildUnaryTree(split.nonLiftableFlatten, child));

            return liftAboveConjunct(
                    conjuncts,
                    split.liftableFlatten,
                    updatedChild);
        }
    }

    private static class SplitFlattenSequence {
        private final ImmutableList<FlattenNode> liftableFlatten;
        private final ImmutableList<FlattenNode> nonLiftableFlatten;

        public SplitFlattenSequence(ImmutableList<FlattenNode> liftableFlatten, ImmutableList<FlattenNode> nonLiftableFlatten) {
            this.liftableFlatten = liftableFlatten;
            this.nonLiftableFlatten = nonLiftableFlatten;
        }
    }

    private static class FlattenLifterException extends OntopInternalBugException {
        FlattenLifterException(String message) {
            super(message);
        }
    }

}
