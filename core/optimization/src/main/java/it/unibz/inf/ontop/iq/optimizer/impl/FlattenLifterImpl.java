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
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Lifts flatten nodes up the algebraic tree.
 * ASSUMPTION: all join conditions are EXPLICIT.
 *
 * The procedure may alter the order of flatten nodes.
 *
 * E.g.:
 * <p>
 * filter(A' = C'')
 * flatten1 (A -> A')
 * flatten2 (B -> B')
 * flatten3 (C' -> C'')
 * flatten4 (D -> D')
 * flatten5 (C -> C')
 * table(A,B,C,D)
 *
 * Note that:
 * - flatten1 and flatten3 cannot be lifted over the filter.
 * - flatten5 cannot be lifted over flatten3.
 * <p>
 * The procedure yields:
 * <p>
 * flatten4 (D -> D')
 * flatten2 (B -> B')
 * filter(A' = A'')
 * flatten3 (C' -> C'')
 * flatten5 (C -> C')
 * flatten1 (A -> A')
 * table(A,B,C,D)
 * <p>
 *
 * For efficiency, the procedure tries to lift consecutive flatten nodes in one pass.
 * More precisely, the procedure f takes a query Q, with root N.
 * - base case (N is a data node): f(Q) = Q
 * - inductive case:
 *  for each child Q' of Q:
 *      a) compute f(Q')
 *      b) try to lift above N all consecutive flatten nodes at the root of f(Q').
 *
 *  Step b) may:
 *  - leave some flatten nodes below N
 *  - reorder flatten nodes
 *
 * The behavior for potentially blocking operators is the following:
 *
 * - filter:
 * . lift all flatten nodes that can be lifted above the filter
 * . if a conjunct of the filter condition blocks the lift of a flatten node, then the filter may be split.
 * E.g.:
 * filter (A' = 1 && B' = 2)
 * flatten (A -> A')
 * becomes:
 * filter(A' = 1)
 * flatten (A -> A')
 * filter(B' = 2)
 *
 * - inner join:
 * . the explicit join condition is isolated as a filter,
 * . flatten nodes are systematically lifted above the join (and below the filter)
 * . the procedure for a lift above the filter is applied
 * . if a filter-join sequence has been produced, it may be simplified (as a join with an explicit join condition).
 *
 * - left join:
 * . the explicit join condition is never lifted.
 * . flatten nodes from the right-hand-side are not lifted
 * . flatten nodes from the left-hand-side are lifted if they do not define a variable used in the left join condition
 *
 * - construction node:
 * . flatten nodes are lifted if they do not define a variable used in the substitution's range.
 * . the construction node's projected variables are updated accordingly
 */
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

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode filter, IQTree child) {
            child = child.acceptTransformer(this);

            ImmutableList<FlattenNode> flattens = getConsecutiveFlatten(child)
                    .collect(ImmutableCollectors.toList());

            child = discardRootFlattenNodes(child, flattens.iterator());

            ImmutableList<QueryNode> seq = liftRec(
                    concat(
                            flattens.reverse().stream(),
                            Stream.of(filter)
                    ),
                    flattens.size(),
                    child.getVariables(),
                    ImmutableSet.of()
            );
            return buildUnaryTreeRec(
                    seq.reverse().iterator(),
                    child
            );
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode cn, IQTree child) {
            child = child.acceptTransformer(this);
            if (tree.getRootNode().equals(cn)) {
                return iqFactory.createUnaryIQTree(cn, child);
            }
            ImmutableList<FlattenNode> flattens = getConsecutiveFlatten(child)
                    .collect(ImmutableCollectors.toList());

            child = discardRootFlattenNodes(child, flattens.iterator());
            ImmutableList<QueryNode> seq = liftRec(
                    concat(
                            flattens.reverse().stream(),
                            Stream.of(cn)
                    ),
                    flattens.size(),
                    child.getVariables(),
                    ImmutableSet.of()
            );
            return buildUnaryTreeRec(
                    seq.reverse().iterator(),
                    child
            );
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode lj, IQTree leftChild, IQTree rightChild) {

            leftChild = leftChild.acceptTransformer(this);
            rightChild = rightChild.acceptTransformer(this);

            ImmutableSet<Variable> implicitJoinVariables = getImplicitJoinVariables(ImmutableList.of(leftChild, rightChild));

            ImmutableList<FlattenNode> flattens = getConsecutiveFlatten(leftChild)
                    .collect(ImmutableCollectors.toList());

            leftChild = discardRootFlattenNodes(leftChild, flattens.iterator());
            ImmutableList<QueryNode> seq = liftRec(
                    concat(
                            flattens.reverse().stream(),
                            Stream.of(lj)
                    ),
                    flattens.size(),
                    leftChild.getVariables(),
                    implicitJoinVariables
            );

            SplitLJLift lift = splitLJLift(seq);
            return buildUnaryTreeRec(
                    lift.getLiftedNodes().reverse().iterator(),
                    iqFactory.createBinaryNonCommutativeIQTree(
                            lj,
                            buildUnaryTreeRec(
                                    lift.getNonLiftedNodes().iterator(),
                                    leftChild
                            ),
                            rightChild
                    ));
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode join, ImmutableList<IQTree> children) {
            children = children.stream()
                    .map(c -> c.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ImmutableList<FlattenNode>> flattenLists = children.stream()
                    .map(t -> getConsecutiveFlatten(t).collect(ImmutableCollectors.toList()))
                    .collect(ImmutableCollectors.toList());

            UnmodifiableIterator<ImmutableList<FlattenNode>> it = flattenLists.iterator();

            children = children.stream()
                    .map(t -> discardRootFlattenNodes(t, it.next().iterator()))
                    .collect(ImmutableCollectors.toList());
            ImmutableList flattens = flattenLists.stream()
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toList());

            if (join.getOptionalFilterCondition().isPresent()) {
                FilterNode filter = iqFactory.createFilterNode(join.getOptionalFilterCondition().get());
                ImmutableList<QueryNode> seq = liftRec(
                        concat(
                                flattens.reverse().stream(),
                                Stream.of(filter)
                        ),
                        flattens.size(),
                        children.stream()
                                .flatMap(t -> t.getVariables().stream())
                                .collect(ImmutableCollectors.toSet()),
                        ImmutableSet.of()
                );
                // avoid a consecutive join+filter
                InnerJoinNode updatedJoin;
                if (seq.get(0) instanceof FilterNode) {
                    updatedJoin = iqFactory.createInnerJoinNode((((FilterNode) seq.get(0)).getFilterCondition()));
                    seq = seq.subList(1, seq.size());
                } else {
                    updatedJoin = iqFactory.createInnerJoinNode();
                }
                return buildUnaryTreeRec(
                        seq.reverse().iterator(),
                        iqFactory.createNaryIQTree(
                                updatedJoin,
                                children
                        )
                );
            }
            return buildUnaryTreeRec(
                    flattens.iterator(),
                    iqFactory.createNaryIQTree(
                            join,
                            children
                    )
            );
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

        private Stream<FlattenNode> getConsecutiveFlatten(IQTree tree) {
            QueryNode n = tree.getRootNode();
            if (n instanceof FlattenNode) {
                return Stream.concat(
                        Stream.of((FlattenNode) n),
                        getConsecutiveFlatten(((UnaryIQTree) tree).getChild())
                );
            }
            return Stream.of();
        }

        private ImmutableList<QueryNode> liftRec(ImmutableList<QueryNode> seq, int parentIndex, ImmutableSet<Variable> subtreeVars, ImmutableSet blockingVars) {
            if (parentIndex == 0) {
                return seq;
            }
            seq = liftAboveParentRec(seq, parentIndex, subtreeVars, blockingVars);
            return liftRec(seq, parentIndex - 1, subtreeVars, blockingVars);
        }

        private ImmutableList<QueryNode> liftAboveParentRec(ImmutableList<QueryNode> seq, int parentIndex, ImmutableSet<Variable> subTreeVars, ImmutableSet<Variable> blockingVars) {
            if (parentIndex == seq.size()) {
                return seq;
            }
            if (!(seq.get(parentIndex - 1) instanceof FlattenNode)) {
                throw new FlattenLifterException("A Flatten Node is expected");
            }
            FlattenNode flatten = (FlattenNode) seq.get(parentIndex - 1);
            QueryNode parent = seq.get(parentIndex);
            // Variables defined by the flatten node (and not in its subtree)
            ImmutableSet<Variable> definedVars = flatten.getLocallyDefinedVariables();
            ImmutableList<QueryNode> lift = getChildParentLift(flatten, parent, definedVars, blockingVars);
            if (lift.isEmpty()) {
                return seq;
            }
            ImmutableList<QueryNode> init = seq.subList(0, parentIndex - 1);
            ImmutableList<QueryNode> tail = seq.subList(parentIndex + 1, seq.size());
            return liftAboveParentRec(
                    concat(
                            init.stream(),
                            lift.stream(),
                            tail.stream()),
                    parentIndex + 1,
                    subTreeVars,
                    blockingVars
            );
        }

        private ImmutableList<QueryNode> getChildParentLift(FlattenNode liftedNode, QueryNode parent, ImmutableSet<Variable> definedVars, ImmutableSet<Variable> blockingVars) {
            if (parent instanceof FlattenNode) {
                return getLift(liftedNode, (FlattenNode) parent, definedVars);
            }
            if (parent instanceof FilterNode) {
                return getLift(liftedNode, (FilterNode) parent, definedVars);
            }
            if (parent instanceof ConstructionNode) {
                return getLift(liftedNode, (ConstructionNode) parent, definedVars);
            }
            if (parent instanceof LeftJoinNode) {
                return getLift(liftedNode, (LeftJoinNode) parent, definedVars, blockingVars);
            }
            throw new FlattenLifterException("A Filter, Flatten Left Join or Construction Node is expected");
        }

        private ImmutableList<QueryNode> getLift(FlattenNode liftedNode, LeftJoinNode parent, ImmutableSet<Variable> definedVars, ImmutableSet<Variable> blockingVars) {
            if(definedVars.stream()
                    .anyMatch(v -> blockingVars.contains(v))){
                return ImmutableList.of();
            }
            if (parent.getOptionalFilterCondition().isPresent()) {
                ImmutableSet<Variable> joinConditionVars = parent.getOptionalFilterCondition().get().getVariables();
                if (definedVars.stream()
                        .anyMatch(v -> joinConditionVars.contains(v))) {
                    return ImmutableList.of();
                }
            }
            return ImmutableList.of(parent, liftedNode);
        }

        private ImmutableList<QueryNode> getLift(FlattenNode liftedNode, FlattenNode parent, ImmutableSet<Variable> definedVars) {
            if (definedVars.contains(parent.getFlattenedVariable())) {
                return ImmutableList.of();
            }
            return ImmutableList.of(parent, liftedNode);
        }

        private ImmutableList<QueryNode> getLift(FlattenNode liftedNode, FilterNode parent, ImmutableSet<Variable> definedVars) {
            SplitExpression split = new SplitExpression(
                    definedVars,
                    parent.getFilterCondition());
            Optional<ImmutableExpression> nonLiftedExpr = split.getNonLiftedExpression();
            if (nonLiftedExpr.isPresent()) {
                Optional<ImmutableExpression> liftedExpr = split.getLiftedExpression();
                if (liftedExpr.isPresent()) {
                    return ImmutableList.of(
                            iqFactory.createFilterNode(nonLiftedExpr.get()),
                            liftedNode,
                            iqFactory.createFilterNode(liftedExpr.get())
                    );
                }
                return ImmutableList.of(
                        parent,
                        liftedNode
                );
            }
            return ImmutableList.of();
        }

        private ImmutableList<QueryNode> getLift(FlattenNode liftedNode, ConstructionNode parent, ImmutableSet<Variable> definedVars) {
            ImmutableSet<Variable> varsInSubRange = getVarsInSubstitutionRange(parent);

            if (definedVars.stream()
                    .anyMatch(v -> varsInSubRange.contains(v))) {
                return ImmutableList.of();
            }
            // among variables projected by the cn, delete the ones defined by the fn, and add the flattened variable
            ImmutableSet<Variable> projectedVars =
                    Stream.concat(
                            parent.getVariables().stream()
                                    .filter(v -> definedVars.contains(v)),
                            Stream.of(liftedNode.getFlattenedVariable())
                    ).collect(ImmutableCollectors.toSet());

            ConstructionNode updatedCn = iqFactory.createConstructionNode(projectedVars, parent.getSubstitution());
            return ImmutableList.of(updatedCn, liftedNode);
        }

        private ImmutableList<QueryNode> concat(Stream<? extends QueryNode>... streams) {
            return Arrays.stream(streams)
                    .flatMap(s -> s)
                    .collect(ImmutableCollectors.toList());
        }

        private ImmutableSet<Variable> getImplicitJoinVariables(ImmutableList<IQTree> children) {
            return children.stream()
                    .flatMap(t -> t.getVariables().stream())
                    .collect(ImmutableCollectors.toMultiset()).entrySet().stream()
                    .filter(e -> e.getCount() > 1)
                    .map(Multiset.Entry::getElement)
                    .collect(ImmutableCollectors.toSet());
        }

        private ImmutableSet<Variable> getVarsInSubstitutionRange(ConstructionNode cn) {
            return cn.getSubstitution().getImmutableMap().values().stream()
                    .flatMap(t -> t.getVariableStream())
                    .collect(ImmutableCollectors.toSet());
        }

        private IQTree buildUnaryTreeRec(Iterator<? extends QueryNode> it, IQTree subtree) {
            if (it.hasNext()) {
                QueryNode n = it.next();
                if (n instanceof UnaryOperatorNode) {
                    return iqFactory.createUnaryIQTree(
                            (UnaryOperatorNode) n,
                            buildUnaryTreeRec(it, subtree)
                    );
                }
                throw new FlattenLiftException(n + "is not a unary node");
            }
            return subtree;
        }

        private SplitLJLift splitLJLift(ImmutableList<QueryNode> seq) {
            ImmutableList.Builder<FlattenNode> liftedNodes = ImmutableList.builder();
            ImmutableList.Builder<FlattenNode> nonLiftedNodes = ImmutableList.builder();
            LeftJoinNode lj = null;
            boolean lifted = false;
            UnmodifiableIterator<QueryNode> it = seq.reverse().iterator();
            while (it.hasNext() && !lifted) {
                QueryNode n = it.next();
                if (n instanceof FlattenNode) {
                    liftedNodes.add((FlattenNode) n);
                } else if (n instanceof LeftJoinNode) {
                    lj = (LeftJoinNode) n;
                    lifted = true;
                } else throw new FlattenLiftException("A Flatten of Left Join node is expected");
            }
            while (it.hasNext()) {
                QueryNode n = it.next();
                if (n instanceof FlattenNode) {
                    nonLiftedNodes.add((FlattenNode) n);
                } else throw new FlattenLiftException("A Flatten node is expected");
            }
            return new SplitLJLift(liftedNodes.build(), nonLiftedNodes.build(), lj);
        }

        private class SplitExpression {
            private final ImmutableSet<ImmutableExpression> nonLiftedConjuncts;
            private final ImmutableSet<ImmutableExpression> liftedConjuncts;

            private SplitExpression(ImmutableSet<Variable> liftVars, ImmutableExpression expr) {
                this(liftVars, expr.flattenAND());
            }

            private SplitExpression(ImmutableSet<Variable> liftVars, Stream<ImmutableExpression> conjuncts) {
                ImmutableMap<Boolean, ImmutableList<ImmutableExpression>> splitMap = splitConjuncts(liftVars, conjuncts);
                nonLiftedConjuncts = ImmutableSet.copyOf(splitMap.get(false));
                liftedConjuncts = ImmutableSet.copyOf(splitMap.get(true));
            }

            /**
             * Partitions the conjuncts of the input expression:
             * - conjuncts containing no variable in vars
             * - conjuncts containing some variable in vars
             */
            private ImmutableMap<Boolean, ImmutableList<ImmutableExpression>> splitConjuncts(ImmutableSet<Variable> vars, Stream<ImmutableExpression> conjuncts) {
                return conjuncts
                        .collect(ImmutableCollectors.partitioningBy(e -> e.getVariableStream()
                                .anyMatch(vars::contains)));
            }

            Optional<ImmutableExpression> getNonLiftedExpression() {
                return nonLiftedConjuncts.isEmpty() ?
                        Optional.empty() :
                        termFactory.getConjunction(nonLiftedConjuncts.stream());
            }

            Optional<ImmutableExpression> getLiftedExpression() {
                return liftedConjuncts.isEmpty() ?
                        Optional.empty() :
                        termFactory.getConjunction(liftedConjuncts.stream());
            }
        }

        private class FlattenLiftException extends OntopInternalBugException {
            FlattenLiftException(String message) {
                super(message);
            }
        }

        private class SplitLJLift {

            private final ImmutableList<FlattenNode> liftedNodes;
            private final ImmutableList<FlattenNode> nonLiftedNodes;
            private final LeftJoinNode lj;

            private SplitLJLift(ImmutableList<FlattenNode> liftedNodes, ImmutableList<FlattenNode> nonLiftedNodes, LeftJoinNode lj) {
                this.liftedNodes = liftedNodes;
                this.nonLiftedNodes = nonLiftedNodes;
                this.lj = lj;
            }

            ImmutableList<FlattenNode> getLiftedNodes() {
                return liftedNodes;
            }

            ImmutableList<FlattenNode> getNonLiftedNodes() {
                return nonLiftedNodes;
            }

            public LeftJoinNode getLj() {
                return lj;
            }
        }
    }

    private static class FlattenLifterException extends OntopInternalBugException {
        FlattenLifterException(String message) {
            super(message);
        }
    }

}
