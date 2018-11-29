package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.FlattenLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
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
 *  flatten1 (A -> [A1,A2])
 *   flatten2 (B -> [B1,B2])
 *    flatten3 (C1 -> [C3,C4])
 *     flatten4 (D -> [D1,D2])
 *      flatten5 (C -> [C1,C2])
 *       table(A,B,C,D)
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
 *  flatten4 (D -> [D1,D2])
 *   filter(A1 = 2 & C3 =3)
 *    flatten1 (A -> [A1,A2])
 *     flatten3 (C1 -> [C3,C4])
 *       flatten5 (C -> [C1,C2])
 *        table(A,B,C,D)
 */
public class FlattenLifterImpl implements FlattenLifter {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private FlattenLifterImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
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
            QueryNode childNode = child.getRootNode();
            if (childNode instanceof FlattenNode) {
                FlattenLift lift = liftFlattenSequence(
                        (FlattenNode) childNode,
                        new HashSet<>(),
                        fn.getFilterCondition().getVariables(),
                        ((UnaryIQTree) child).getChild()
                );
                if (!lift.getLiftableNodes().isEmpty()) {
                    return buildUnaryTreeRec(
                            ImmutableList.<UnaryOperatorNode>builder().addAll(lift.getLiftableNodes()).add(fn).build().reverse().iterator(),
                            lift.getSubtree()
                    );
                }
            }
            return iqFactory.createUnaryIQTree(fn, child);
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode cn, IQTree child) {
            child = child.acceptTransformer(this);
            QueryNode childNode = child.getRootNode();
            if (childNode instanceof FlattenNode) {
                ImmutableSubstitution sub = cn.getSubstitution();
                FlattenLift lift = liftFlattenSequence(
                        (FlattenNode) childNode,
                        getVarsInSubRange(sub),
                        ImmutableSet.of(),
                        ((UnaryIQTree) child).getChild()
                );
                if (!lift.getLiftableNodes().isEmpty()) {
                    return buildUnaryTreeRec(
                            ImmutableList.<UnaryOperatorNode>builder()
                                    .addAll(applySubstitution(lift.getLiftableNodes(), sub))
                                    .add(cn).build().reverse().iterator(),
                            lift.getSubtree()
                    );
                }
            }
            return iqFactory.createUnaryIQTree(cn, child);
        }

        private HashSet<Variable> getVarsInSubRange(ImmutableSubstitution sub) {
            return (HashSet<Variable>) sub.getImmutableMap().values().stream()
                    .flatMap(t -> ((ImmutableTerm)t).getVariableStream())
                    .collect(Collectors.toCollection(HashSet::new));
        }

        private Iterable<? extends UnaryOperatorNode> applySubstitution(ImmutableList<FlattenNode> liftableNodes, ImmutableSubstitution sub) {
            return liftableNodes.stream()
                    .map(n -> applySubstitution(sub,n))
                    .collect(ImmutableCollectors.toList());
        }

        /**
         * @param blockingVars: if the current flatten node's data atom uses one of these var, then the node cannot be lifted
         * @param blockingIfExclusiveVars: if the current flatten node's data atom uses one of these var, and the subtree does not project it, then the node cannot be lifted
         */
        private FlattenLift liftFlattenSequence(FlattenNode fn, HashSet<Variable> blockingVars, ImmutableSet<Variable> blockingIfExclusiveVars, IQTree child) {
            FlattenLift childLift;
            if (child.getRootNode() instanceof FlattenNode) {
                blockingVars.add(fn.getArrayVariable());
                childLift = liftFlattenSequence((FlattenNode) child.getRootNode(), blockingVars, blockingIfExclusiveVars, ((UnaryIQTree) child).getChild());
            } else {
                childLift = new FlattenLift(ImmutableList.of(), child);
            }

            if (isLiftable(fn, blockingVars, blockingIfExclusiveVars, child)) {
                return new FlattenLift(
                        ImmutableList.<FlattenNode>builder().add(fn).addAll(childLift.getLiftableNodes()).add(fn).build(),
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
            if(it.hasNext()) {
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

        private boolean isLiftable(FlattenNode fn, HashSet<Variable> blockingVars, ImmutableSet<Variable> blockingIfExclusiveVars, IQTree child) {
            ImmutableSet<Variable> dataAtomExlcusiveVars = getDataAtomExclusiveVars(fn, child);
            if(dataAtomExlcusiveVars.stream().anyMatch(blockingIfExclusiveVars::contains)) {
                return false;
            }
            return fn.getDataAtom().getVariables().stream().anyMatch(blockingVars::contains);
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

            IQTree getSubtree() {
                return subtree;
            }
        }

        private class FlattenLiftException extends OntopInternalBugException {
            FlattenLiftException(String message) {
                super(message);
            }
        }

    }
}
