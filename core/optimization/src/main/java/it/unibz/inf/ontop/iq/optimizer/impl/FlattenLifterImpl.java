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
 * Lifts flatten nodes.
 * <p>
 * Main difficulty: sequence of consecutive flatten nodes.
 * Consider the (sub)tree S, composed of a potentially blocking operator (e.g. filter),
 * and a sequence of flatten, e.g.:
 * <p>
 * Ex: filter(A1 = C3)
 * flatten1 (A -> [A1,A2])
 * flatten2 (B -> [B1,B2])
 * flatten3 (C1 -> [C3,C4])
 * flatten4 (D -> [D1,D2])
 * flatten5 (C -> [C1,C2])
 * table(A,B,C,D)
 * <p>
 * Note that:
 * - flatten1 and flatten3 cannot be lifted over the filter.
 * - flatten5 cannot be lifted over flatten3.
 * <p>
 * Solution:
 * - apply the optimization to the child tree first (in this example, it consists of the data node only).
 * - within S, lift each flatten node one after the other, as high as possible, starting with the 1st flatten (from root to leaf).
 * Note that the order of flatten operators may change as a result.
 * <p>
 * This yields:
 * <p>
 * flatten4 (D -> [D1,D2])
 * flatten2 (B -> [B1,B2])
 * filter(A1 = C3)
 * flatten3 (C1 -> [C3,C4])
 * flatten5 (C -> [C1,C2])
 * flatten1 (A -> [A1,A2])
 * table(A,B,C,D)
 * <p>
 * <p>
 * This is complicated by the potential split of boolean expressions.
 * E.g. in the previous example, let the filter expression be (A1 = 2) && (C3 = 3)
 * <p>
 * Then some conjuncts of the expression may be lifted together with a flatten.
 * This is performed only if at least one conjunct is not lifted.
 * <p>
 * E.g. lifting flatten 1 first, we get:
 * <p>
 * filter(A1 = 2)
 * flatten1 (A -> [A1,A2])
 * filter(C3 = 3)
 * flatten2 (B -> [B1,B2])
 * flatten4 (D -> [D1,D2])
 * flatten3 (C1 -> [C3,C4])
 * flatten5 (C -> [C1,C2])
 * table(A,B,C,D)
 * <p>
 * Then lifting flatten 2:
 * <p>
 * flatten2 (B -> [B1,B2])
 * filter(A1 = 2)
 * flatten1 (A -> [A1,A2])
 * filter(C3 = 3)
 * flatten4 (D -> [D1,D2])
 * flatten3 (C1 -> [C3,C4])
 * flatten5 (C -> [C1,C2])
 * table(A,B,C,D)
 * <p>
 * Then flatten 4:
 * <p>
 * flatten4 (D -> [D1,D2])
 * flatten2 (B -> [B1,B2])
 * filter(A1 = 2)
 * flatten1 (A -> [A1,A2])
 * filter(C3 = 3)
 * flatten3 (C1 -> [C3,C4])
 * flatten5 (C -> [C1,C2])
 * table(A,B,C,D)
 * <p>
 * The behavior for the other operators is the following:
 *
 * - inner join:
 * . the the explicit join condition is isolated as a filter,
 * . flatten nodes are systematically lifted above the join (and below the filter)
 * . the procedure for a lift above the filter is applied
 * . the (possibly) resulting filter-join sequence is replaced by a join with explicit join condition.
 *
 * - left join:
 * . the explicit join condition is never lifted.
 * . flatten nodes from the right-hand-side are not lifted
 * . flatten nodes from the left-hand-side are lifted if they do not define a variable used in the left join condition
 *
 * - construction node:
 * . flatten nodes are lifted if they do not define a variable used ion the substitution range.
 * . the substitution is applied to the lifted flatten nodes' array variables (could happen in theory in the construction node renames the array variable)
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
//        IQ prev;
        //do {
        //  prev = query;
//            query = iqFactory.createIQ(
//                    query.getProjectionAtom(),
//                    query.getTree().acceptTransformer(treeTransformer)
//            );
        //} while (!prev.equals(query));
//        return query;
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
            ImmutableSet<Variable> definedVars = getDefinedVariables(flatten, seq, parentIndex - 1, subTreeVars);
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
            ImmutableSet<Variable> arrayDefinedVars = (ImmutableSet<Variable>) liftedNode.getDataAtom().getVariables().stream()
                    .filter(v -> !definedVars.contains(v))
                    .collect(ImmutableCollectors.toSet());
            if (arrayDefinedVars.stream()
                    .anyMatch(v -> varsInSubRange.contains(v))) {
                return ImmutableList.of();
            }
            // among variables projected by the cn, delete the ones defined in the fn's array, and add the fn's array variable
            ImmutableSet<Variable> projectedVars =
                    Stream.concat(
                            parent.getVariables().stream()
                                    .filter(v -> arrayDefinedVars.contains(v)),
                            Stream.of(liftedNode.getFlattenedVariable())
                    ).collect(ImmutableCollectors.toSet());

            // apply the substitution to the flatten node's array variable (if applicable, which is unlikely)
            ConstructionNode updatedCn = iqFactory.createConstructionNode(projectedVars, parent.getSubstitution());
            FlattenNode updatedFlatten = applySubstitution(parent.getSubstitution(), liftedNode);
            return ImmutableList.of(updatedCn, updatedFlatten);
        }



        private ImmutableSet<Variable> getDefinedVariables(FlattenNode flatten, ImmutableList<QueryNode> seq, int index, ImmutableSet<Variable> subtreeVars) {

            ImmutableSet<Variable> subsequenceVars = (ImmutableSet<Variable>) seq.subList(0, index).stream()
                    .filter(n -> n instanceof FlattenNode)
                    .flatMap(n -> ((FlattenNode) n).getDataAtom().getVariables().stream())
                    .collect(ImmutableCollectors.toSet());

            return (ImmutableSet<Variable>)
                    flatten.getDataAtom().getVariables().stream()
                            .filter(v -> !subtreeVars.contains(v))
                            .filter(v -> !subsequenceVars.contains(v))
                            .collect(ImmutableCollectors.toSet());
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

        private FlattenNode applySubstitution(ImmutableSubstitution substitution, FlattenNode flattenNode) {

            Variable arrayVar = Optional.of(
                    substitution.apply(flattenNode.getFlattenedVariable()))
                    .filter(v -> v instanceof Variable)
                    .map(v -> (Variable) v)
                    .orElseThrow(() -> new FlattenLiftException("Applying this substitution is expected to yield a variable." +
                            "\nSubstitution: " + substitution +
                            "\nApplied to: " + substitution
                    ));

            return flattenNode.newNode(
                    arrayVar,
                    flattenNode.getArrayIndexIndex(),
                    flattenNode.getDataAtom()
            );
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
