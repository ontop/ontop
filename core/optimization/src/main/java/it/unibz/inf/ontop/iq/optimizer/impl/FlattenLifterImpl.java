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
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lifts flatten nodes.
 * <p>
 * Main difficulty: sequence S of consecutive flatten nodes.
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
 * - apply the optimization to the child tree first
 * - within S, lift only flatten nodes which can be lifted above the first non-flatten node (in this case, above the filter)
 * <p>
 * This yields:
 * <p>
 * flatten2 (B -> [B1,B2])
 * flatten4 (D -> [D1,D2])
 * filter(A1 = C3)
 * flatten1 (A -> [A1,A2])
 * flatten3 (C1 -> [C3,C4])
 * flatten5 (C -> [C1,C2])
 * table(A,B,C,D)
 * <p>
 * <p>
 * Another technical aspect is the potential split of boolean expressions.
 * E.g. in the previous example, let the filter expression be (A1 = 2) && (C3 = 3)
 * <p>
 * Then if we split the expression into conjuncts, either flatten1 or (flatten3 and flatten5) can be lifted:
 * E.g. (lifting flatten 1):
 * <p>
 * flatten2 (B -> [B1,B2])
 * flatten4 (D -> [D1,D2])
 * filter(A1 = 2)
 * flatten1 (A -> [A1,A2])
 * filter(C3 = 3)
 * flatten3 (C1 -> [C3,C4])
 * flatten5 (C -> [C1,C2])
 * table(A,B,C,D)
 * <p>
 * The behavior for the different operators is the following:
 * - inner join: (sequences of) flatten nodes are systematically lifted above the join
 * Note that this may cause a cross product (TODO: optimize so as to avoid cross products?)
 * Some conjuncts of the explicit join condition may be lifted above all flatten nodes, as a filter.
 * Then a lift above the filter is performed, which may split the expression further.
 * - left join: the explicit join condition is never lifted.
 * - filter: the expression is split into conjuncts, as above.
 * The expression split is done in a greedy way, looking for the first liftable flatten node (e.g. in the example above, flatten1 is lifted, rather than flatten 3 and flatten5).
 * TODO: optimize by lifting as many flatten as possible?
 */
public class FlattenLifterImpl implements FlattenLifter {

    private final IntermediateQueryFactory iqFactory;
    private final ImmutabilityTools immutabilityTools;


    @Inject
    private FlattenLifterImpl(IntermediateQueryFactory iqFactory, ImmutabilityTools immutabilityTools) {
        this.iqFactory = iqFactory;
        this.immutabilityTools = immutabilityTools;
    }

    @Override
    public IQ optimize(IQ query) {
        TreeTransformer treeTransformer = new TreeTransformer(iqFactory);
        IQ prev;
        do {
            prev = query;
            query = iqFactory.createIQ(
                    query.getProjectionAtom(),
                    query.getTree().acceptTransformer(treeTransformer)
            );
        } while (!prev.equals(query));
        return query;
    }

    private class TreeTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        TreeTransformer(IntermediateQueryFactory iqFactory) {
            super(iqFactory);
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode filter, IQTree child) {
            child = child.acceptTransformer(this);
            FlattenLift lift = getFlattenLift(
                    getBlockingVariables(filter.getFilterCondition()),
                    child
            );
            if (lift.getLiftableNodes().isEmpty())
                return iqFactory.createUnaryIQTree(filter, child);

            return buildUnaryTreeRec(
                    interleave(Optional.of(filter.getFilterCondition()), lift).iterator(),
                    lift.getSubtree()
            );
        }

        private ImmutableList<UnaryOperatorNode> interleave(Optional<ImmutableExpression> expr, FlattenLift lift) {
            Iterator<FlattenNode> fns = lift.getLiftableNodes().iterator();
            ImmutableList.Builder<UnaryOperatorNode> builder = ImmutableList.builder();
            if (expr.isPresent()) {
                SplitExpression split = new SplitExpression(ImmutableSet.of(), expr.get());
                while (split.getNonLiftedExpression().isPresent() && fns.hasNext()) {
                    FlattenNode fn = fns.next();
                    split = new SplitExpression(lift.getDefinedVariables(fn), expr.get());
                    if (split.getLiftedExpression().isPresent()) {
                        builder.add(iqFactory.createFilterNode(split.getLiftedExpression().get()));
                    }
                    builder.add(fn);
                }
            }
            while (fns.hasNext()) {
                builder.add(fns.next());
            }
            return builder.build();
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode join, ImmutableList<IQTree> children) {
            children = children.stream()
                    .map(c -> c.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());
            ImmutableList<FlattenLift> lifts = getFlattenLifts(ImmutableSet.of(), children);
            if (lifts.stream()
                    .anyMatch(l -> !l.getLiftableNodes().isEmpty())) {
                ImmutableList<FlattenNode> liftedNodes = lifts.stream()
                        .flatMap(l -> l.getLiftableNodes().stream())
                        .collect(ImmutableCollectors.toList());
                IQTree subtree = iqFactory.createNaryIQTree(
                        iqFactory.createInnerJoinNode(),
                        lifts.stream()
                                .map(l -> l.getSubtree())
                                .collect(ImmutableCollectors.toList())
                );
                FlattenLift combinedLift = new FlattenLift(liftedNodes, subtree);
                Optional<ImmutableExpression> joinCondition = join.getOptionalFilterCondition();
                ImmutableList<UnaryOperatorNode> operators = interleave(joinCondition, combinedLift);
                return buildUnaryTreeRec(
                        operators.iterator(),
                        combinedLift.getSubtree()
                );
            }
            return iqFactory.createNaryIQTree(join, children);
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode lj, IQTree leftChild, IQTree rightChild) {
            ImmutableList<IQTree> children = ImmutableList.of(
                    leftChild.acceptTransformer(this),
                    rightChild.acceptTransformer(this)
            );

            ImmutableSet.Builder<Variable> blockingVars = ImmutableSet.builder();
            blockingVars.addAll(getImplicitJoinVariables(children));
            lj.getOptionalFilterCondition()
                    .ifPresent(e -> blockingVars.addAll(e.getVariables()));

            ImmutableList<FlattenLift> flattenLifts = getFlattenLifts(blockingVars.build(), children);
            if (flattenLifts.stream()
                    .anyMatch(l -> !l.getLiftableNodes().isEmpty())) {
                return buildUnaryTreeRec(
                        flattenLifts.stream()
                                .flatMap(l -> l.getLiftableNodes().stream()).iterator(),
                        iqFactory.createBinaryNonCommutativeIQTree(
                                lj,
                                flattenLifts.get(0).getSubtree(),
                                flattenLifts.get(1).getSubtree()
                        ));
            }
            return iqFactory.createBinaryNonCommutativeIQTree(lj, children.get(0), children.get(1));
        }

        private ImmutableSet<Variable> getImplicitJoinVariables(ImmutableList<IQTree> children) {
            return children.stream()
                    .flatMap(t -> t.getVariables().stream())
                    .collect(ImmutableCollectors.toMultiset()).entrySet().stream()
                    .filter(e -> e.getCount() > 1)
                    .map(Multiset.Entry::getElement)
                    .collect(ImmutableCollectors.toSet());
        }

//        private SplitExpression splitExpression(UnmodifiableIterator<FlattenLift> iterator, Optional<ImmutableExpression> expr) {
//            if (expr.isPresent()) {
//                if (iterator.hasNext()) {
//                    FlattenLift lift = iterator.next();
//                    SplitExpression recSplit = splitExpression(iterator, expr);
//                    SplitExpression split = new SplitExpression(lift.getDefinedVariables(), expr);
//                    // Merge the two splits: union of lifted conjuncts, and intersection of non lifted ones
//                    return new SplitExpression(
//                            ImmutableSet.copyOf(Sets.union(
//                                    recSplit.liftedConjuncts,
//                                    split.liftedConjuncts)),
//                            recSplit.nonLiftedConjuncts.stream()
//                                    .filter(c -> split.nonLiftedConjuncts.contains(c))
//                                    .collect(ImmutableCollectors.toSet())
//                    );
//                }
//            }
//            return new SplitExpression(ImmutableSet.of(), expr);
//        }

        private FlattenLift getFlattenLift(ImmutableSet<Variable> blockingVariables, IQTree child) {
            QueryNode n = child.getRootNode();
            if (n instanceof FlattenNode) {
                return liftFlattenSequence(
                        (FlattenNode) n,
                        new HashSet<>(),
                        blockingVariables,
                        Optional.empty(),
                        ((UnaryIQTree) child).getChild()
                );
            }
            return new FlattenLift(ImmutableList.of(), child);
        }

        private ImmutableList<FlattenLift> getFlattenLifts(ImmutableSet<Variable> blockingVars, ImmutableList<IQTree> children) {
            return children.stream()
                    .map(t -> getFlattenLift(blockingVars, t))
                    .collect(ImmutableCollectors.toList());
        }

        /**
         * Returns variables appearing in all conjuncts of the expression
         */
        private ImmutableSet<Variable> getBlockingVariables(ImmutableExpression expr) {
            ImmutableSet<ImmutableExpression> conjuncts = expr.flattenAND();
            ImmutableSet<Variable> firstConjunctVars = conjuncts.iterator().next().getVariables();
            return conjuncts.stream()
                    .flatMap(c -> c.getVariableStream())
                    .filter(v -> firstConjunctVars.contains(v))
                    .collect(ImmutableCollectors.toSet());
        }


        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode cn, IQTree child) {
            child = child.acceptTransformer(this);
            QueryNode childNode = child.getRootNode();
            if (childNode instanceof FlattenNode) {
                ImmutableSubstitution sub = cn.getSubstitution();
                FlattenLift lift = liftFlattenSequence(
                        (FlattenNode) childNode,
                        getVarsInSubstitutionRange(sub),
                        cn.getVariables(),
                        Optional.of(cn.getVariables()),
                        ((UnaryIQTree) child).getChild()
                );
                if (!lift.getLiftableNodes().isEmpty()) {
                    return buildUnaryTreeRec(
                            ImmutableList.<UnaryOperatorNode>builder()
                                    .addAll(applySubstitution(lift.getLiftableNodes(), sub))
                                    .add(cn).build().iterator(),
                            lift.getSubtree()
                    );
                }
            }
            return iqFactory.createUnaryIQTree(cn, child);
        }

        private HashSet<Variable> getVarsInSubstitutionRange(ImmutableSubstitution sub) {
            return (HashSet<Variable>) sub.getImmutableMap().values().stream()
                    .flatMap(t -> ((ImmutableTerm) t).getVariableStream())
                    .collect(Collectors.toCollection(HashSet::new));
        }

        private Iterable<? extends UnaryOperatorNode> applySubstitution(ImmutableList<FlattenNode> flattenNodes,
                                                                        ImmutableSubstitution sub) {
            return flattenNodes.stream()
                    .map(n -> applySubstitution(sub, n))
                    .collect(ImmutableCollectors.toList());
        }

        /**
         * @param blockingVars:            if the flatten node's data atom uses one of these var, then the node cannot be lifted
         * @param blockingIfExclusiveVars: if the flatten node's data atom uses one of these var, and the subtree does not project it, then the node cannot be lifted
         * @param projectedVars:           if present, and if the flatten node's array variable is NOT one of these, then the node cannot be lifted
         */
        private FlattenLift liftFlattenSequence(FlattenNode fn, HashSet<Variable> blockingVars, ImmutableSet<Variable> blockingIfExclusiveVars,
                                                Optional<ImmutableSet<Variable>> projectedVars,
                                                IQTree child) {
            FlattenLift childLift;
            if (child.getRootNode() instanceof FlattenNode) {
                blockingVars.add(fn.getArrayVariable());
                childLift = liftFlattenSequence((FlattenNode) child.getRootNode(), blockingVars, blockingIfExclusiveVars, projectedVars, ((UnaryIQTree) child).getChild());
            } else {
                childLift = new FlattenLift(ImmutableList.of(), child);
            }

            if (isLiftable(fn, blockingVars, blockingIfExclusiveVars, projectedVars, child)) {
                return new FlattenLift(
                        ImmutableList.<FlattenNode>builder().add(fn).addAll(childLift.getLiftableNodes()).build(),
                        childLift.getSubtree()
                );
            }
            return new FlattenLift(
                    childLift.getLiftableNodes(),
                    iqFactory.createUnaryIQTree(
                            fn,
                            childLift.getSubtree()
                    ));
        }

        private IQTree buildUnaryTreeRec(Iterator<? extends UnaryOperatorNode> it, IQTree subtree) {
            if (it.hasNext()) {
                return iqFactory.createUnaryIQTree(
                        it.next(),
                        buildUnaryTreeRec(it, subtree)
                );
            }
            return subtree;
        }

        private FlattenNode applySubstitution(ImmutableSubstitution substitution, FlattenNode flattenNode) {

            Variable arrayVar = Optional.of(
                    substitution.apply(flattenNode.getArrayVariable()))
                    .filter(v -> v instanceof Variable)
                    .map(v -> (Variable) v)
                    .orElseThrow(() -> new FlattenLiftException("Applying this substitution is expected to yield a variable." +
                            "\nSubstitution: " + substitution +
                            "\nApplied to: " + substitution
                    ));

            return flattenNode.newNode(
                    arrayVar,
                    flattenNode.getArrayIndexIndex(),
                    flattenNode.getDataAtom(),
                    flattenNode.getArgumentNullability()
            );
        }

        private boolean isLiftable(FlattenNode fn, HashSet<Variable> blockingVars, ImmutableSet<Variable> blockingIfExclusiveVars, Optional<ImmutableSet<Variable>> projectedVars, IQTree child) {
            ImmutableSet<Variable> dataAtomExlcusiveVars = getDataAtomExclusiveVars(fn, child);
            if (dataAtomExlcusiveVars.stream().anyMatch(blockingIfExclusiveVars::contains))
                return false;
            if (fn.getDataAtom().getVariables().stream().anyMatch(blockingVars::contains))
                return false;
            return projectedVars.map(variables -> variables.contains(fn.getArrayVariable())).orElse(true);
        }

        private ImmutableSet<Variable> getDataAtomExclusiveVars(FlattenNode fn, IQTree child) {
            ImmutableSet<Variable> childVars = child.getVariables();
            return (ImmutableSet<Variable>) fn.getDataAtom().getVariables().stream()
                    .filter(v -> !childVars.contains(v))
                    .collect(ImmutableCollectors.toSet());
        }

        private class FlattenLift {
            private final ImmutableList<FlattenNode> liftableNodes;
            private final IQTree subtree;

            private FlattenLift(ImmutableList<FlattenNode> liftableNodes, IQTree subtree) {
                this.liftableNodes = liftableNodes;
                this.subtree = subtree;
            }

            ImmutableList<FlattenNode> getLiftableNodes() {
                return liftableNodes;
            }

            /**
             * Variables defined by some of the lifted flatten nodes (and not in the subtree)
             */
            ImmutableSet<Variable> getDefinedVariables() {
                return liftableNodes.stream()
                        .flatMap(n -> getDefinedVariables(n).stream())
                        .collect(ImmutableCollectors.toSet());
            }

            /**
             * Variables defined by the flatten node (and not in the subtree)
             */
            ImmutableSet<Variable> getDefinedVariables(FlattenNode fn) {
                return (ImmutableSet<Variable>)
                        fn.getDataAtom().getVariables().stream()
                                .filter(v -> !subtree.getVariables().contains(v))
                                .collect(ImmutableCollectors.toSet());
            }

            IQTree getSubtree() {
                return subtree;
            }
        }

        private class SplitExpression {
            private final ImmutableSet<ImmutableExpression> nonLiftedConjuncts;
            private final ImmutableSet<ImmutableExpression> liftedConjuncts;

            private SplitExpression(ImmutableSet<Variable> liftVars, ImmutableExpression expr) {
                this(liftVars, expr.flattenAND());
            }

            private SplitExpression(ImmutableSet<Variable> liftVars, ImmutableSet<ImmutableExpression> conjuncts) {
                ImmutableMap<Boolean, ImmutableList<ImmutableExpression>> splitMap = splitConjuncts(liftVars, conjuncts);
                nonLiftedConjuncts = ImmutableSet.copyOf(splitMap.get(false));
                liftedConjuncts = ImmutableSet.copyOf(splitMap.get(true));
            }

//            private SplitExpression(ImmutableSet<ImmutableExpression> liftedConjuncts,
//                                    ImmutableSet<ImmutableExpression> nonliftedConjuncts) {
//                this.liftedConjuncts = liftedConjuncts;
//                this.nonLiftedConjuncts = nonliftedConjuncts;
//            }

            /**
             * Partitions the conjuncts of the input expression:
             * - conjuncts containing no variable in vars
             * - conjuncts containing some variable in vars
             */
            private ImmutableMap<Boolean, ImmutableList<ImmutableExpression>> splitConjuncts(ImmutableSet<Variable> vars, ImmutableSet<ImmutableExpression> conjuncts) {
                return conjuncts.stream()
                        .collect(ImmutableCollectors.partitioningBy(e -> e.getVariableStream()
                                .anyMatch(vars::contains)));
            }

            public Optional<ImmutableExpression> getNonLiftedExpression() {
                return nonLiftedConjuncts.isEmpty() ?
                        Optional.empty() :
                        immutabilityTools.foldBooleanExpressions(nonLiftedConjuncts.stream());
            }

            public Optional<ImmutableExpression> getLiftedExpression() {
                return liftedConjuncts.isEmpty() ?
                        Optional.empty() :
                        immutabilityTools.foldBooleanExpressions(liftedConjuncts.stream());
            }
        }

        private class FlattenLiftException extends OntopInternalBugException {
            FlattenLiftException(String message) {
                super(message);
            }
        }
    }

}
