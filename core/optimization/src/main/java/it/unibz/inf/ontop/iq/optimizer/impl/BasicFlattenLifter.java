package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicFlattenLifter implements FlattenLifter {

    private final IntermediateQueryFactory iqFactory;


    @Inject
    private BasicFlattenLifter(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        TreeTransformer treeTransformer = new TreeTransformer(iqFactory);
        return iqFactory.createIQ(
                query.getProjectionAtom(),
                query.getTree().acceptTransformer(treeTransformer)
        );
    }

    private static class TreeTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        TreeTransformer(IntermediateQueryFactory iqFactory) {
            super(iqFactory);
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            child = child.acceptTransformer(this);

            ImmutableList<FlattenNode> flattenNodes = getRootFlattenNodes(child);
            if (flattenNodes.isEmpty()) {
                return iqFactory.createUnaryIQTree(
                        rootNode,
                        child
                );
            }
            child = discardRootFlattenNodes(child, flattenNodes.iterator());
            return liftAboveConjunct(
                    rootNode.getFilterCondition().flattenAND()
                            .collect(Collectors.toCollection(LinkedList::new)),
                    flattenNodes,
                    child
            );
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode cn, IQTree child) {
            child = child.acceptTransformer(this);
            if (tree.getRootNode().equals(cn)) {
                return iqFactory.createUnaryIQTree(cn, child);
            }

            ImmutableList<FlattenNode> flattenNodes = getRootFlattenNodes(child);

            child = discardRootFlattenNodes(child, flattenNodes.iterator());

            SplitFlattenSequence splitFlattenSequence = splitFlattenSequence(
                    flattenNodes,
                    ImmutableSet.of()
            );
            return buildUnaryTree(
                    splitFlattenSequence.liftableFlatten.iterator(),
                    iqFactory.createUnaryIQTree(
                            updateConstructionNode(cn, splitFlattenSequence.liftableFlatten.iterator()),
                            buildUnaryTree(
                                    splitFlattenSequence.nonLiftableFlatten.iterator(),
                                    child
                            )));
        }

        /**
         * Assumption: the join carries no (explicit) joining condition
         */
        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode join, ImmutableList<IQTree> children) {
            children = children.stream()
                    .map(c -> c.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ImmutableList<FlattenNode>> flattenLists = children.stream()
                    .map(this::getRootFlattenNodes)
                    .collect(ImmutableCollectors.toList());

            ImmutableSet<Variable> blockingVars = getImplicitJoinCondition(children);

            Iterator<ImmutableList<FlattenNode>> it = flattenLists.iterator();

            children = children.stream()
                    .map(t -> discardRootFlattenNodes(t, it.next().iterator()))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<SplitFlattenSequence> flattenSequences = flattenLists.stream()
                    .map(l -> splitFlattenSequence(l, blockingVars))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<FlattenNode> liftedFlattenNodes = flattenSequences.stream()
                    .flatMap(fs -> fs.liftableFlatten.stream())
                    .collect(ImmutableCollectors.toList());

            Iterator<SplitFlattenSequence> its = flattenSequences.iterator();

            ImmutableList<IQTree> updatedChildren = children.stream()
                    .map(t -> buildUnaryTree(
                            its.next().nonLiftableFlatten.iterator(),
                            t
                    ))
                    .collect(ImmutableCollectors.toList());

            return buildUnaryTree(
                    liftedFlattenNodes.iterator(),
                    iqFactory.createNaryIQTree(
                            join,
                            updatedChildren
                    ));
        }

        /**
         * For now the implementation is identical to the one of inner joins, with the exception that all variables involved in the LJ condition are blocking.
         * TODO: identify additional cases where flatten nodes could be lifted, by creating a filter
         * (note that this only possible if extra integrity constraints hold, or in the presence of a distinct)
         */
        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            ImmutableList<IQTree> children = ImmutableList.of(leftChild, rightChild);

            children = children.stream()
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

            children = children.stream()
                    .map(t -> discardRootFlattenNodes(t, it.next().iterator()))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<SplitFlattenSequence> flattenSequences = flattenLists.stream()
                    .map(l -> splitFlattenSequence(l, blockingVars))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<FlattenNode> liftedFlattenNodes = flattenSequences.stream()
                    .flatMap(fs -> fs.liftableFlatten.stream())
                    .collect(ImmutableCollectors.toList());

            Iterator<SplitFlattenSequence> its = flattenSequences.iterator();

            ImmutableList<IQTree> updatedChildren = children.stream()
                    .map(t -> buildUnaryTree(
                            its.next().nonLiftableFlatten.iterator(),
                            t
                    ))
                    .collect(ImmutableCollectors.toList());

            return buildUnaryTree(
                    liftedFlattenNodes.iterator(),
                    iqFactory.createBinaryNonCommutativeIQTree(
                            rootNode,
                            updatedChildren.get(0),
                            updatedChildren.get(1)
                    ));
        }

        private ConstructionNode updateConstructionNode(ConstructionNode cn, Iterator<FlattenNode> it) {
            if (it.hasNext()) {
                FlattenNode fn = it.next();
                ImmutableSet<Variable> definedVars = fn.getLocallyDefinedVariables();
                // among variables projected by the cn, delete the ones defined by the fn, and add the flattened variable
                ImmutableSet<Variable> projectedVars =
                        Stream.concat(
                                cn.getVariables().stream()
                                        .filter(v -> !definedVars.contains(v)),
                                Stream.of(fn.getFlattenedVariable())
                        ).collect(ImmutableCollectors.toSet());

                ConstructionNode updatedCn = iqFactory.createConstructionNode(projectedVars, cn.getSubstitution());
                return updateConstructionNode(updatedCn, it);
            }
            return cn;
        }

        private SplitFlattenSequence splitFlattenSequence(List<FlattenNode> flattenNodes, ImmutableSet<Variable> blockingVars) {
            return split(new SplitFlattenSequence(), flattenNodes.iterator(), blockingVars);
        }

        private SplitFlattenSequence split(SplitFlattenSequence splitFlattenSequence,
                                           Iterator<FlattenNode> it,
                                           ImmutableSet<Variable> blockingVars) {
            if (it.hasNext()) {
                FlattenNode flattenNode = it.next();

                if (flattenNode.getLocallyDefinedVariables().stream()
                        .anyMatch(blockingVars::contains)) {
                    splitFlattenSequence.appendNonLiftable(flattenNode);
                    blockingVars = ImmutableSet.<Variable>builder()
                            .add(flattenNode.getFlattenedVariable())
                            .addAll(blockingVars)
                            .build();
                } else {
                    splitFlattenSequence.appendLiftable(flattenNode);
                }
                return split(splitFlattenSequence, it, blockingVars);
            }
            return splitFlattenSequence;
        }

        private IQTree buildUnaryTree(Iterator<FlattenNode> it, IQTree subtree) {
            if (it.hasNext()) {
                return iqFactory.createUnaryIQTree(
                        it.next(),
                        buildUnaryTree(it, subtree)
                );
            }
            return subtree;
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

        private IQTree discardRootFlattenNodes(IQTree tree, UnmodifiableIterator<FlattenNode> it) {
            if (it.hasNext()) {
                FlattenNode node = it.next();
                if (tree.getRootNode().equals(node)) {
                    return discardRootFlattenNodes(
                            ((UnaryIQTree) tree).getChild(),
                            it
                    );
                }
                throw new FlattenLifterException("Node " + node + " is expected top be the root of " + tree);
            }
            return tree;
        }

        private ImmutableList<FlattenNode> getRootFlattenNodes(IQTree tree) {
            return getRootFlattenNodesRec(
                    tree,
                    ImmutableList.builder()
            ).build();
        }

        private ImmutableList.Builder<FlattenNode> getRootFlattenNodesRec(IQTree tree, ImmutableList.Builder<FlattenNode> builder) {
            QueryNode n = tree.getRootNode();
            if (n instanceof FlattenNode) {
                builder.add((FlattenNode) n);
                return getRootFlattenNodesRec(((UnaryIQTree) tree).getChild(), builder);
            }
            return builder;
        }

        private IQTree liftAboveConjunct(LinkedList<ImmutableExpression> conjuncts, List<FlattenNode> flattenNodes, IQTree child) {
            if (conjuncts.isEmpty()) {
                return buildUnaryTree(flattenNodes.iterator(), child);
            }

            ImmutableExpression conjunct = conjuncts.removeFirst();
            SplitFlattenSequence split = splitFlattenSequence(flattenNodes, conjunct.getVariables());
            child = iqFactory.createUnaryIQTree(
                    iqFactory.createFilterNode(conjunct),
                    buildUnaryTree(
                            split.nonLiftableFlatten.iterator(),
                            child
                    ));
            return liftAboveConjunct(
                    conjuncts,
                    split.liftableFlatten,
                    child
            );
        }
    }

    private static class SplitFlattenSequence {
        private final List<FlattenNode> liftableFlatten;
        private final List<FlattenNode> nonLiftableFlatten;

        public SplitFlattenSequence() {
            this.liftableFlatten = new LinkedList<>();
            this.nonLiftableFlatten = new LinkedList<>();
        }

        private void appendLiftable(FlattenNode flattenNode) {
            this.liftableFlatten.add(flattenNode);
        }

        private void appendNonLiftable(FlattenNode flattenNode) {
            this.nonLiftableFlatten.add(flattenNode);
        }
    }

    private static class FlattenLifterException extends OntopInternalBugException {
        FlattenLifterException(String message) {
            super(message);
        }
    }
}
