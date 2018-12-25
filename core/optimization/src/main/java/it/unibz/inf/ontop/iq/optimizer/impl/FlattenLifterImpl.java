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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lifts flatten nodes.
 * <p>
 * Difficulty: sequence S of consecutive flatten nodes.
 * <p>
 * Ex: filter(A1 = 2 && C3 =3)
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
 * - consecutive liftable flatten nodes are lifted together (as a block)
 * <p>
 * Illustration:
 * - do not lift 5 over 4, as 5 is (transitively) not liftable over the filter
 * - then lift 4 over 3
 * - then do not lift 4 over 2, as both are liftable over the filter
 * - then lift 2 and 4 (together) over 1
 * - then lift 2 and 4 (together) over the filter
 * This yields:
 * <p>
 * flatten2 (B -> [B1,B2])
 * flatten4 (D -> [D1,D2])
 * filter(A1 = 2 & C3 =3)
 * flatten1 (A -> [A1,A2])
 * flatten3 (C1 -> [C3,C4])
 * flatten5 (C -> [C1,C2])
 * table(A,B,C,D)
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
        public IQTree transformFilter(IQTree tree, FilterNode fn, IQTree child) {
            child = child.acceptTransformer(this);
            FlattenLift lift = getFlattenLift(
                    getBlockingVariables(fn.getFilterCondition()),
                    child
            );
            if (!lift.getLiftableNodes().isEmpty()) {
                SplitExpression split = new SplitExpression(lift.getDefinedVariables(), fn.getOptionalFilterCondition());
                // If some conjunct in the filter expression has no variable provided by the flatten node(s), perform the lift
                Optional<ImmutableExpression> nonLiftedExpr = split.getNonLiftedExpression();
                if (nonLiftedExpr.isPresent()) {
                    ImmutableList.Builder<UnaryOperatorNode> builder = ImmutableList.builder();
                    // Part of the filter condition may be lifted together with the flatten node(s)
                    split.getLiftedExpression().ifPresent(e -> builder.add(iqFactory.createFilterNode(e)));
                    builder.addAll(lift.liftableNodes);
                    builder.add(iqFactory.createFilterNode(nonLiftedExpr.get()));
                    return buildUnaryTreeRec(
                            builder.build().iterator(),
                            lift.getSubtree()
                    );
                }
            }
            return iqFactory.createUnaryIQTree(fn, child);
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode join, ImmutableList<IQTree> children) {
            children = children.stream()
                    .map(c -> c.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            //ImmutableSet<Variable> blockingVars = getImplicitJoinVariables(children);
            ImmutableList<FlattenLift> flattenLifts = getFlattenLifts(ImmutableSet.of(), children);
            if (flattenLifts.stream()
                    .anyMatch(l -> !l.getLiftableNodes().isEmpty())) {
                SplitExpression split = splitExpression(flattenLifts.iterator(), join.getOptionalFilterCondition());
                Optional<ImmutableExpression> expr = split.getNonLiftedExpression();
                InnerJoinNode updatedJoin = expr.isPresent() ?
                        iqFactory.createInnerJoinNode(expr.get()) :
                        iqFactory.createInnerJoinNode();

                ImmutableList<FlattenNode> liftedNodes = flattenLifts.stream()
                        .flatMap(l -> l.getLiftableNodes().stream())
                        .collect(ImmutableCollectors.toList());

                ImmutableList.Builder<UnaryOperatorNode> builder = ImmutableList.builder();
                // Part of the filter condition may be lifted together with the flatten node(s)
                split.getLiftedExpression().ifPresent(e -> builder.add(iqFactory.createFilterNode(e)));
                builder.addAll(liftedNodes);
                return buildUnaryTreeRec(
                        builder.build().iterator(),
                        iqFactory.createNaryIQTree(
                                updatedJoin,
                                flattenLifts.stream()
                                        .map(l -> l.getSubtree())
                                        .collect(ImmutableCollectors.toList())
                        ));
            }
            return iqFactory.createNaryIQTree(join, children);
        }

        private ImmutableSet<Variable> getImplicitJoinVariables(ImmutableList<IQTree> children) {
            return children.stream()
                    .flatMap(t -> t.getVariables().stream())
                    .collect(ImmutableCollectors.toMultiset()).entrySet().stream()
                    .filter(e -> e.getCount() > 1)
                    .map(Multiset.Entry::getElement)
                    .collect(ImmutableCollectors.toSet());
        }

        private SplitExpression splitExpression(UnmodifiableIterator<FlattenLift> iterator, Optional<ImmutableExpression> expr) {
            if (expr.isPresent()) {
                if (iterator.hasNext()) {
                    FlattenLift lift = iterator.next();
                    SplitExpression recSplit = splitExpression(iterator, expr);
                    SplitExpression split = new SplitExpression(lift.getDefinedVariables(), expr);
                    // Merge the two splits: union of lifted conjuncts, and intersection of non lifted ones
                    return new SplitExpression(
                            ImmutableSet.copyOf(Sets.union(
                                    recSplit.liftedConjuncts,
                                    split.liftedConjuncts)),
                            recSplit.nonLifedConjuncts.stream()
                                    .filter(c -> split.nonLifedConjuncts.contains(c))
                                    .collect(ImmutableCollectors.toSet())
                    );
                }
            }
            return new SplitExpression(ImmutableSet.of(), expr);
        }

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

        private Iterable<? extends UnaryOperatorNode> applySubstitution (ImmutableList<FlattenNode> flattenNodes,
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

        private IQTree buildUnaryTreeRec(UnmodifiableIterator<UnaryOperatorNode> it, IQTree subtree) {
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
            if(fn.getDataAtom().getVariables().stream().anyMatch(blockingVars::contains))
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
                return (ImmutableSet<Variable>) liftableNodes.stream()
                        .flatMap(n -> n.getDataAtom().getVariables().stream())
                        .filter(v -> !subtree.getVariables().contains(v))
                        .collect(ImmutableCollectors.toSet());
            }

            IQTree getSubtree() {
                return subtree;
            }
        }

        private class SplitExpression {
            private final ImmutableSet<ImmutableExpression> nonLifedConjuncts;
            private final ImmutableSet<ImmutableExpression> liftedConjuncts;

            private SplitExpression(ImmutableSet<Variable> liftVars, Optional<ImmutableExpression> expr) {

                if (expr.isPresent()) {
                    ImmutableMap<Boolean, ImmutableList<ImmutableExpression>> splitMap = splitExpression(liftVars, expr.get());
                    nonLifedConjuncts = ImmutableSet.copyOf(splitMap.get(false));
                    liftedConjuncts = ImmutableSet.copyOf(splitMap.get(true));
                } else {
                    nonLifedConjuncts = ImmutableSet.of();
                    liftedConjuncts = ImmutableSet.of();
                }
            }

            private SplitExpression(ImmutableSet<ImmutableExpression> liffedConjuncts,
                                    ImmutableSet<ImmutableExpression> nonliftedConjuncts) {
                this.liftedConjuncts = liffedConjuncts;
                this.nonLifedConjuncts = nonliftedConjuncts;
            }

            /**
             * Partitions the conjuncts of the input expression:
             * - conjuncts containing no variable in vars
             * - conjuncts containing some variable in vars
             */
            private ImmutableMap<Boolean, ImmutableList<ImmutableExpression>> splitExpression(ImmutableSet<Variable> vars, ImmutableExpression expr) {
                return expr.flattenAND().stream()
                        .collect(ImmutableCollectors.partitioningBy(e -> e.getVariableStream()
                                .anyMatch(vars::contains)));
            }

            public ImmutableSet<ImmutableExpression> getNonLifedConjuncts() {
                return nonLifedConjuncts;
            }

            public ImmutableSet<ImmutableExpression> getLiftedConjuncts() {
                return liftedConjuncts;
            }

            public Optional<ImmutableExpression> getNonLiftedExpression() {
                return nonLifedConjuncts.isEmpty() ?
                        Optional.empty() :
                        immutabilityTools.foldBooleanExpressions(nonLifedConjuncts.stream());
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
