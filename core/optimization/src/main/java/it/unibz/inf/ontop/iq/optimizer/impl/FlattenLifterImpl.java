package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.optimizer.FlattenLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class FlattenLifterImpl implements FlattenLifter {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;


    @Inject
    private FlattenLifterImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        TreeTransformer treeTransformer = new TreeTransformer(iqFactory);
        return iqFactory.createIQ(
                query.getProjectionAtom(),
                query.getTree().acceptTransformer(treeTransformer)
        );
    }

    private class TreeTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        TreeTransformer(IntermediateQueryFactory iqFactory) {
            super(iqFactory);
        }

//        @Override
//        public IQTree transformFilter(IQTree tree, FilterNode filter, IQTree child) {
//            child = child.acceptTransformer(this);
//
//            ImmutableList<FlattenNode> flattens = getConsecutiveFlatten(child)
//                    .collect(ImmutableCollectors.toList());
//
//            child = discardRootFlattenNodes(child, flattens.iterator());
//
//            ImmutableList<UnaryOperatorNode> seq = lift(
//                    concat(
//                            flattens.reverse().stream(),
//                            Stream.of(filter)
//                    ),
//                    flattens.size(),
//                    ImmutableSet.of()
//            );
//            return buildUnaryTreeOld(
//                    seq.reverse().iterator(),
//                    child
//            );
//        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode cn, IQTree child) {
            child = child.acceptTransformer(this);
            if (tree.getRootNode().equals(cn)) {
                return iqFactory.createUnaryIQTree(cn, child);
            }

            ImmutableList<FlattenNode> flattenNodes = getRootFlattenNodes(child)
                    .collect(ImmutableCollectors.toList());

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

        private SplitFlattenSequence splitFlattenSequence(ImmutableList<FlattenNode> flattenNodes, ImmutableSet<Variable> blockingVars) {
            return split(new SplitFlattenSequence(), flattenNodes.iterator(), blockingVars);
        }

        private SplitFlattenSequence split(SplitFlattenSequence splitFlattenSequence,
                                           UnmodifiableIterator<FlattenNode> it,
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

//        @Override
//        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode lj, IQTree leftChild, IQTree rightChild) {
//
//            leftChild = leftChild.acceptTransformer(this);
//            rightChild = rightChild.acceptTransformer(this);
//
//            ImmutableSet<Variable> implicitJoinVariables = getImplicitJoinVariables(ImmutableList.of(leftChild, rightChild));
//
//            ImmutableList<FlattenNode> flattens = getRootFlattenNodes(leftChild)
//                    .collect(ImmutableCollectors.toList());
//
//            leftChild = discardRootFlattenNodes(leftChild, flattens.iterator());
//            ImmutableList<QueryNode> seq = lift(
//                    concat(
//                            flattens.reverse().stream(),
//                            Stream.of(lj)
//                    ),
//                    flattens.size(),
//                    implicitJoinVariables
//            );
//
//            SplitLJLift lift = splitLJLift(seq);
//            return buildUnaryTreeOld(
//                    lift.getLiftedNodes().reverse().iterator(),
//                    iqFactory.createBinaryNonCommutativeIQTree(
//                            lj,
//                            buildUnaryTreeOld(
//                                    lift.getNonLiftedNodes().iterator(),
//                                    leftChild
//                            ),
//                            rightChild
//                    ));
//        }

        /**
         * Assumption: the join carries no (explicit) joining condition
         */
        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode join, ImmutableList<IQTree> children) {
            children = children.stream()
                    .map(c -> c.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ImmutableList<FlattenNode>> flattenLists = children.stream()
                    .map(t -> getRootFlattenNodes(t).collect(ImmutableCollectors.toList()))
                    .collect(ImmutableCollectors.toList());

            ImmutableSet<Variable> blockingVars = retrieveImplicitJoinCondition(children);

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

        private ImmutableSet<Variable> retrieveImplicitJoinCondition(ImmutableList<IQTree> children) {
            return children.stream()
                    .flatMap(n -> n.getVariables().stream())
                    .collect(ImmutableCollectors.toMultiset())
                    .entrySet().stream()
                    .filter(e -> e.getCount() > 1)
                    .map(e -> e.getElement())
                    .collect(ImmutableCollectors.toSet());
        }
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

    private Stream<FlattenNode> getRootFlattenNodes(IQTree tree) {
        QueryNode n = tree.getRootNode();
        if (n instanceof FlattenNode) {
            return Stream.concat(
                    getRootFlattenNodes(((UnaryIQTree) tree).getChild()),
                    Stream.of((FlattenNode) n)
            );
        }
        return Stream.of();
    }

//        private ImmutableList<UnaryOperatorNode> lift(ImmutableList<UnaryOperatorNode> seq, int parentIndex, ImmutableSet blockingVars) {
//            if (parentIndex == 0) {
//                return seq;
//            }
//            seq = liftAboveParent(seq, parentIndex, blockingVars);
//            return lift(seq, parentIndex - 1, blockingVars);
//        }

//        private ImmutableList<UnaryOperatorNode> liftAboveParent(ImmutableList<UnaryOperatorNode> seq, int parentIndex,  ImmutableSet<Variable> blockingVars) {
//            if (parentIndex == seq.size()) {
//                return seq;
//            }
//            if (!(seq.get(parentIndex - 1) instanceof FlattenNode)) {
//                throw new FlattenLifterException("A Flatten Node is expected");
//            }
//            FlattenNode flatten = (FlattenNode) seq.get(parentIndex - 1);
//            QueryNode parent = seq.get(parentIndex);
//            // Variables defined by the flatten node (and not in its subtree)
//            ImmutableSet<Variable> definedVars = flatten.getLocallyDefinedVariables();
//            ImmutableList<UnaryOperatorNode> lift = getChildParentLift(flatten, parent, definedVars, blockingVars);
//            if (lift.isEmpty()) {
//                return seq;
//            }
//            ImmutableList<UnaryOperatorNode> init = seq.subList(0, parentIndex - 1);
//            ImmutableList<UnaryOperatorNode> tail = seq.subList(parentIndex + 1, seq.size());
//            return liftAboveParent(
//                    concat(
//                            init.stream(),
//                            lift.stream(),
//                            tail.stream()),
//                    parentIndex + 1,
//                    blockingVars
//            );
//        }

//        private ImmutableList<UnaryOperatorNode> getChildParentLift(FlattenNode liftedNode, QueryNode parent, ImmutableSet<Variable> definedVars, ImmutableSet<Variable> blockingVars) {
//            if (parent instanceof FlattenNode) {
//                return getLift(liftedNode, (FlattenNode) parent, definedVars);
//            }
//            if (parent instanceof FilterNode) {
//                return getLift(liftedNode, (FilterNode) parent, definedVars);
//            }
//            if (parent instanceof ConstructionNode) {
//                return getLift(liftedNode, (ConstructionNode) parent, definedVars);
//            }
//            if (parent instanceof LeftJoinNode) {
//                return getLift(liftedNode, (LeftJoinNode) parent, definedVars, blockingVars);
//            }
//            throw new FlattenLifterException("A Filter, Flatten Left Join or Construction Node is expected");
//        }

//        private ImmutableList<QueryNode> getLift(FlattenNode liftedNode, LeftJoinNode parent, ImmutableSet<Variable> definedVars, ImmutableSet<Variable> blockingVars) {
//            if(definedVars.stream()
//                    .anyMatch(v -> blockingVars.contains(v))){
//                return ImmutableList.of();
//            }
//            if (parent.getOptionalFilterCondition().isPresent()) {
//                ImmutableSet<Variable> joinConditionVars = parent.getOptionalFilterCondition().get().getVariables();
//                if (definedVars.stream()
//                        .anyMatch(v -> joinConditionVars.contains(v))) {
//                    return ImmutableList.of();
//                }
//            }
//            return ImmutableList.of(parent, liftedNode);
//        }

//        private ImmutableList<UnaryOperatorNode> getLift(FlattenNode liftedNode, FlattenNode parent, ImmutableSet<Variable> definedVars) {
//            if (definedVars.contains(parent.getFlattenedVariable())) {
//                return ImmutableList.of();
//            }
//            return ImmutableList.of(parent, liftedNode);
//        }

//        private ImmutableList<QueryNode> getLift(FlattenNode liftedNode, FilterNode parent, ImmutableSet<Variable> definedVars) {
//            SplitExpression split = new SplitExpression(
//                    definedVars,
//                    parent.getFilterCondition());
//            Optional<ImmutableExpression> nonLiftedExpr = split.getNonLiftedExpression();
//            if (nonLiftedExpr.isPresent()) {
//                Optional<ImmutableExpression> liftedExpr = split.getLiftedExpression();
//                if (liftedExpr.isPresent()) {
//                    return ImmutableList.of(
//                            iqFactory.createFilterNode(nonLiftedExpr.get()),
//                            liftedNode,
//                            iqFactory.createFilterNode(liftedExpr.get())
//                    );
//                }
//                return ImmutableList.of(
//                        parent,
//                        liftedNode
//                );
//            }
//            return ImmutableList.of();
//        }

//        private ImmutableList<QueryNode> getLift(FlattenNode liftedNode, ConstructionNode parent, ImmutableSet<Variable> definedVars) {
//            ImmutableSet<Variable> varsInSubRange = getVarsInSubstitutionRange(parent);
//
//            if (definedVars.stream()
//                    .anyMatch(v -> varsInSubRange.contains(v))) {
//                return ImmutableList.of();
//            }
//            // among variables projected by the cn, delete the ones defined by the fn, and add the flattened variable
//            ImmutableSet<Variable> projectedVars =
//                    Stream.concat(
//                            parent.getVariables().stream()
//                                    .filter(v -> definedVars.contains(v)),
//                            Stream.of(liftedNode.getFlattenedVariable())
//                    ).collect(ImmutableCollectors.toSet());
//
//            ConstructionNode updatedCn = iqFactory.createConstructionNode(projectedVars, parent.getSubstitution());
//            return ImmutableList.of(updatedCn, liftedNode);
//        }

//        private ImmutableList<QueryNode> concat(Stream<? extends QueryNode>... streams) {
//            return Arrays.stream(streams)
//                    .flatMap(s -> s)
//                    .collect(ImmutableCollectors.toList());
//        }

//        private ImmutableSet<Variable> getImplicitJoinVariables(ImmutableList<IQTree> children) {
//            return children.stream()
//                    .flatMap(t -> t.getVariables().stream())
//                    .collect(ImmutableCollectors.toMultiset()).entrySet().stream()
//                    .filter(e -> e.getCount() > 1)
//                    .map(Multiset.Entry::getElement)
//                    .collect(ImmutableCollectors.toSet());
//        }

//        private ImmutableSet<Variable> getVarsInSubstitutionRange(ConstructionNode cn) {
//            return cn.getSubstitution().getImmutableMap().values().stream()
//                    .flatMap(t -> t.getVariableStream())
//                    .collect(ImmutableCollectors.toSet());
//        }
//
//        private IQTree buildUnaryTreeOld(Iterator<UnaryOperatorNode> it, IQTree subtree) {
//            if (it.hasNext()) {
//                QueryNode n = it.next();
//                if (n instanceof UnaryOperatorNode) {
//                    return iqFactory.createUnaryIQTree(
//                            (UnaryOperatorNode) n,
//                            buildUnaryTreeOld(it, subtree)
//                    );
//                }
//                throw new FlattenLiftException(n + "is not a unary node");
//            }
//            return subtree;
//        }

//        private SplitLJLift splitLJLift(ImmutableList<QueryNode> seq) {
//            ImmutableList.Builder<FlattenNode> liftedNodes = ImmutableList.builder();
//            ImmutableList.Builder<FlattenNode> nonLiftedNodes = ImmutableList.builder();
//            LeftJoinNode lj = null;
//            boolean lifted = false;
//            UnmodifiableIterator<QueryNode> it = seq.reverse().iterator();
//            while (it.hasNext() && !lifted) {
//                QueryNode n = it.next();
//                if (n instanceof FlattenNode) {
//                    liftedNodes.add((FlattenNode) n);
//                } else if (n instanceof LeftJoinNode) {
//                    lj = (LeftJoinNode) n;
//                    lifted = true;
//                } else throw new FlattenLiftException("A Flatten of Left Join node is expected");
//            }
//            while (it.hasNext()) {
//                QueryNode n = it.next();
//                if (n instanceof FlattenNode) {
//                    nonLiftedNodes.add((FlattenNode) n);
//                } else throw new FlattenLiftException("A Flatten node is expected");
//            }
//            return new SplitLJLift(liftedNodes.build(), nonLiftedNodes.build(), lj);
//        }

//        private class SplitExpression {
//            private final ImmutableSet<ImmutableExpression> nonLiftedConjuncts;
//            private final ImmutableSet<ImmutableExpression> liftedConjuncts;
//
//            private SplitExpression(ImmutableSet<Variable> liftVars, ImmutableExpression expr) {
//                this(liftVars, expr.flattenAND());
//            }
//
//            private SplitExpression(ImmutableSet<Variable> liftVars, Stream<ImmutableExpression> conjuncts) {
//                ImmutableMap<Boolean, ImmutableList<ImmutableExpression>> splitMap = splitConjuncts(liftVars, conjuncts);
//                nonLiftedConjuncts = ImmutableSet.copyOf(splitMap.get(false));
//                liftedConjuncts = ImmutableSet.copyOf(splitMap.get(true));
//            }

//            /**
//             * Partitions the conjuncts of the input expression:
//             * - conjuncts containing no variable in vars
//             * - conjuncts containing some variable in vars
//             */
//            private ImmutableMap<Boolean, ImmutableList<ImmutableExpression>> splitConjuncts(ImmutableSet<Variable> vars, Stream<ImmutableExpression> conjuncts) {
//                return conjuncts
//                        .collect(ImmutableCollectors.partitioningBy(e -> e.getVariableStream()
//                                .anyMatch(vars::contains)));
//            }
//
//            Optional<ImmutableExpression> getNonLiftedExpression() {
//                return nonLiftedConjuncts.isEmpty() ?
//                        Optional.empty() :
//                        termFactory.getConjunction(nonLiftedConjuncts.stream());
//            }
//
//            Optional<ImmutableExpression> getLiftedExpression() {
//                return liftedConjuncts.isEmpty() ?
//                        Optional.empty() :
//                        termFactory.getConjunction(liftedConjuncts.stream());
//            }
//        }

    private static class FlattenLiftException extends OntopInternalBugException {
        FlattenLiftException(String message) {
            super(message);
        }
    }

//        private class SplitLJLift {
//
//            private final ImmutableList<FlattenNode> liftedNodes;
//            private final ImmutableList<FlattenNode> nonLiftedNodes;
//            private final LeftJoinNode lj;
//
//            private SplitLJLift(ImmutableList<FlattenNode> liftedNodes, ImmutableList<FlattenNode> nonLiftedNodes, LeftJoinNode lj) {
//                this.liftedNodes = liftedNodes;
//                this.nonLiftedNodes = nonLiftedNodes;
//                this.lj = lj;
//            }
//
//            ImmutableList<FlattenNode> getLiftedNodes() {
//                return liftedNodes;
//            }
//
//            ImmutableList<FlattenNode> getNonLiftedNodes() {
//                return nonLiftedNodes;
//            }
//
//            public LeftJoinNode getLj() {
//                return lj;
//            }
//        }
//    }

    private static class SplitFlattenSequence {
        private List<FlattenNode> liftableFlatten;
        private List<FlattenNode> nonLiftableFlatten;

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
