package org.semanticweb.ontop.executor.join;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO: explain
 *
 * Assumption: clean inner join structure (an inner join does not have another inner join or filter node as a child).
 *
 */
public class RedundantSelfJoinExecutor implements InternalProposalExecutor<InnerJoinOptimizationProposal> {

    private final class DataAtomClassification {

        private final ImmutableSet<DataNode> nodesToRemove;
        private final ImmutableSet<DataNode> newNodes;

        private DataAtomClassification(Set<DataNode> nodesToRemove, Set<DataNode> newNodes) {
            this.nodesToRemove = ImmutableSet.copyOf(nodesToRemove);
            this.newNodes = ImmutableSet.copyOf(newNodes);
        }

        public ImmutableSet<DataNode> getNodesToRemove() {
            return nodesToRemove;
        }

        public ImmutableSet<DataNode> getNewNodes() {
            return newNodes;
        }
    }


    private final ImmutableMap<AtomPredicate, ImmutableList<Integer>> primaryKeys;

    /**
     * TODO: find a way to get this information
     */
    public RedundantSelfJoinExecutor(ImmutableMap<AtomPredicate, ImmutableList<Integer>> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    @Override
    public NodeCentricOptimizationResults apply(final InnerJoinOptimizationProposal proposal,
                                                final IntermediateQuery query,
                                                final QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {

        ImmutableMultimap<AtomPredicate, DataNode> initialMap = extractDataNodes(query.getChildren(proposal.getTopJoinNode()));

        /**
         * Tries to optimize if there are data nodes
         */
        if (!initialMap.isEmpty()) {

            ImmutableMultimap<AtomPredicate, DataNode> optimizedMap = optimize(initialMap);

            if (optimizedMap != initialMap) {
                // SIDE-EFFECT on the tree component (and thus on the query)
                DataAtomClassification classification = classifyAtoms(initialMap, optimizedMap);
                applyOptimization(treeComponent, proposal.getTopJoinNode(), classification);
            }
        }
        return new NodeCentricOptimizationResultsImpl(query, proposal.getTopJoinNode());
    }

    /**
     * TODO: explain
     */
    private DataAtomClassification classifyAtoms(ImmutableMultimap<AtomPredicate, DataNode> initialMap,
                                      ImmutableMultimap<AtomPredicate, DataNode> optimizedMap) {
        Set<DataNode> nodesToRemove = new HashSet<>();
        Set<DataNode> newNodes = new HashSet<>();

        for (AtomPredicate predicate : initialMap.keySet()) {
            ImmutableCollection<DataNode> initialAtoms = initialMap.get(predicate);
            ImmutableCollection<DataNode> remainingAtoms = optimizedMap.get(predicate);

            for (DataNode initialAtom : initialAtoms) {
                if (!remainingAtoms.contains(initialAtom)) {
                    nodesToRemove.add(initialAtom);
                }
            }
            for (DataNode remainingAtom : remainingAtoms) {
                if (!initialAtoms.contains(remainingAtom)) {
                    newNodes.add(remainingAtom);
                }
            }
        }
        return new DataAtomClassification(nodesToRemove, newNodes);
    }

    private static void applyOptimization(QueryTreeComponent treeComponent, InnerJoinNode topJoinNode,
                                          DataAtomClassification classification) {
        for (DataNode nodeToRemove : classification.getNodesToRemove()) {
            treeComponent.removeSubTree(nodeToRemove);
        }
        for (DataNode newNode : classification.getNewNodes()) {
            try {
                treeComponent.addChild(topJoinNode, newNode, Optional.<ArgumentPosition>absent(), false);
            } catch (IllegalTreeUpdateException e) {
                throw new RuntimeException("Unexpected: " + e.getMessage());
            }
        }
    }


    private ImmutableMultimap<AtomPredicate, DataNode> optimize(ImmutableMultimap<AtomPredicate, DataNode> initialDataNodeMap) {
        ImmutableMultimap.Builder<AtomPredicate, DataNode> mapBuilder = ImmutableMultimap.builder();

        boolean hasOptimize = false;

        for (AtomPredicate predicate : initialDataNodeMap.keySet()) {
            ImmutableCollection<DataNode> initialNodes = initialDataNodeMap.get(predicate);
            ImmutableCollection<DataNode> newNodes;
            if (primaryKeys.containsKey(predicate)) {
                newNodes = optimizePredicate(predicate, initialNodes, primaryKeys.get(predicate));

                if ((!hasOptimize) && (newNodes != initialNodes)) {
                    hasOptimize = true;
                }
            }
            else {
                newNodes = initialNodes;
            }
            mapBuilder.putAll(predicate, newNodes);
        }

        return hasOptimize ? mapBuilder.build() : initialDataNodeMap;
    }

    private static ImmutableCollection<DataNode> optimizePredicate(AtomPredicate predicate,
                                                                   ImmutableCollection<DataNode> dataNodes,
                                                                   ImmutableList<Integer> primaryKeyPositions) {
        throw new RuntimeException("TODO: implement it");
    }


    private static ImmutableMultimap<AtomPredicate, DataNode> extractDataNodes(ImmutableList<QueryNode> siblings) {
        ImmutableMultimap.Builder<AtomPredicate, DataNode> mapBuilder = ImmutableMultimap.builder();
        for (QueryNode node : siblings) {
            if (node instanceof DataNode) {
                DataNode dataNode = (DataNode) node;
                mapBuilder.put(dataNode.getAtom().getPredicate(), dataNode);
            }
        }
        return mapBuilder.build();
    }
}
