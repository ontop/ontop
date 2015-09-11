package org.semanticweb.ontop.executor.join;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.impl.VariableCollector;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Set;

/**
 * TODO: explain
 *
 * Assumption: clean inner join structure (an inner join does not have another inner join or filter node as a child).
 *
 * Naturally assumes that the data atoms are leafs.
 *
 */
public class RedundantSelfJoinExecutor implements InternalProposalExecutor<InnerJoinOptimizationProposal> {

    protected static class PredicateLevelProposal {

        private final ImmutableCollection<DataNode> keptDataNodes;
        private final ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> substitutions;
        private final ImmutableCollection<DataNode> removedDataNodes;

        protected PredicateLevelProposal(ImmutableCollection<DataNode> keptDataNodes,
                                         ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> substitutions,
                                         ImmutableCollection<DataNode> removedDataNodes) {
            this.keptDataNodes = keptDataNodes;
            this.substitutions = substitutions;
            this.removedDataNodes = removedDataNodes;
        }

        /**
         * No redundancy
         */
        protected PredicateLevelProposal(ImmutableCollection<DataNode> initialDataNodes) {
            this.keptDataNodes = initialDataNodes;
            this.substitutions = ImmutableList.of();
            this.removedDataNodes = ImmutableList.of();
        }

        /**
         * Not unified
         */
        public ImmutableCollection<DataNode> getKeptDataNodes() {
            return keptDataNodes;
        }

        public ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> getSubstitutions() {
            return substitutions;
        }

        /**
         * Not unified
         */
        public ImmutableCollection<DataNode> getRemovedDataNodes() {
            return removedDataNodes;
        }

        public boolean shouldOptimize() {
            return !removedDataNodes.isEmpty();
        }
    }

    protected static class ConcreteProposal {

        private final ImmutableCollection<DataNode> transformedDataNodes;
        private final Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution;
        private final ImmutableCollection<DataNode> removedDataNodes;

        protected ConcreteProposal(ImmutableCollection<DataNode> transformedDataNodes,
                                   Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution,
                                   ImmutableCollection<DataNode> removedDataNodes) {
            this.transformedDataNodes = transformedDataNodes;
            this.optionalSubstitution = optionalSubstitution;
            this.removedDataNodes = removedDataNodes;
        }

        public ImmutableCollection<DataNode> getTransformedDataNodes() {
            return transformedDataNodes;
        }

        public ImmutableCollection<DataNode> getDataNodesToRemove() {
            return removedDataNodes;
        }

        public Optional<ImmutableSubstitution<VariableOrGroundTerm>> getOptionalSubstitution() {
            return optionalSubstitution;
        }

        public boolean shouldOptimize() {
            return !removedDataNodes.isEmpty();
        }

    }


    /**
     * TODO: thrown when the upper ConstructionNode would need to be updated.
     * TODO: remove it
     */
    private final class UnsupportedUnificationException extends Exception {

        private UnsupportedUnificationException(String message) {
            super(message);
        }
    }

    /**
     * TODO: explain
     */
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
    public NodeCentricOptimizationResults apply(final InnerJoinOptimizationProposal highLevelProposal,
                                                final IntermediateQuery query,
                                                final QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {

        InnerJoinNode topJoinNode = highLevelProposal.getTopJoinNode();

        ImmutableMultimap<AtomPredicate, DataNode> initialMap = extractDataNodes(query.getChildren(topJoinNode));

        // TODO: explain
        ImmutableSet<Variable> variablesToKeep = VariableCollector.collectVariables(
                query.getClosestConstructionNode(topJoinNode));

        /**
         * Tries to optimize if there are data nodes
         */
        if (!initialMap.isEmpty()) {

            ConcreteProposal concreteProposal = optimize(initialMap, variablesToKeep);

            if (concreteProposal.shouldOptimize()) {
                // SIDE-EFFECT on the tree component (and thus on the query)
                applyOptimization(treeComponent, highLevelProposal.getTopJoinNode(), concreteProposal);
            }
        }
        return new NodeCentricOptimizationResultsImpl(query, topJoinNode);
    }

    private ConcreteProposal optimize(ImmutableMultimap<AtomPredicate, DataNode> initialDataNodeMap,
                                      ImmutableSet<Variable> variablesToKeep) {

        ImmutableList.Builder<PredicateLevelProposal> proposalListBuilder = ImmutableList.builder();

        for (AtomPredicate predicate : initialDataNodeMap.keySet()) {
            ImmutableCollection<DataNode> initialNodes = initialDataNodeMap.get(predicate);
            PredicateLevelProposal predicateProposal;
            if (primaryKeys.containsKey(predicate)) {
                predicateProposal = optimizePredicate(initialNodes, primaryKeys.get(predicate));
            }
            else {
                predicateProposal = new PredicateLevelProposal(initialNodes);
            }
            proposalListBuilder.add(predicateProposal);
        }

        return createConcreteProposal(proposalListBuilder.build(), variablesToKeep);
    }

    /**
     * TODO: explain
     *
     */
    private PredicateLevelProposal optimizePredicate(ImmutableCollection<DataNode> dataNodes,
                                                     ImmutableList<Integer> primaryKeyPositions) {
        final ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMap
                = groupByPrimaryKeyArguments(dataNodes, primaryKeyPositions);

        /**
         * Not yet unified
         */
        ImmutableList.Builder<DataNode> keptDataNodeBuilder = ImmutableList.builder();
        ImmutableList.Builder<DataNode> removedDataNodeBuilder = ImmutableList.builder();

        /**
         * TODO: explain
         */
        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> substitutionBuilder = ImmutableList.builder();

        for (ImmutableList<VariableOrGroundTerm> argumentList : groupingMap.keySet()) {
            ImmutableCollection<DataNode> redundantNodes = groupingMap.get(argumentList);
            switch (redundantNodes.size()) {
                case 0:
                    // Should not happen
                    break;
                case 1:
                    keptDataNodeBuilder.add(redundantNodes.iterator().next());
                default:
                    ImmutableSubstitution<VariableOrGroundTerm> unifier = unifyRedundantNodes(redundantNodes);
                    if (!unifier.isEmpty()) {
                        substitutionBuilder.add(unifier);
                    }

                    UnmodifiableIterator<DataNode> nodeIterator = redundantNodes.iterator();
                    /**
                     *Keeps only the first data node
                     */
                    keptDataNodeBuilder.add(nodeIterator.next());
                    removedDataNodeBuilder.addAll(nodeIterator);
            }
        }
        return new PredicateLevelProposal(keptDataNodeBuilder.build(), substitutionBuilder.build(),
                removedDataNodeBuilder.build());
    }

    /**
     * TODO: explain
     */
    private static ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupByPrimaryKeyArguments(
            ImmutableCollection<DataNode> dataNodes, ImmutableList<Integer> primaryKeyPositions) {
        ImmutableMultimap.Builder<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMapBuilder = ImmutableMultimap.builder();

        for (DataNode dataNode : dataNodes) {
            groupingMapBuilder.put(extractPrimaryKeyArguments(dataNode.getAtom(), primaryKeyPositions), dataNode);
        }
        return groupingMapBuilder.build();
    }

    /**
     * TODO: explain
     */
    private static ImmutableList<VariableOrGroundTerm> extractPrimaryKeyArguments(DataAtom atom,
                                                                                  ImmutableList<Integer> primaryKeyPositions) {
        ImmutableList.Builder<VariableOrGroundTerm> listBuilder = ImmutableList.builder();
        int atomLength = atom.getImmutableTerms().size();

        for (Integer keyIndex : primaryKeyPositions) {
            if (keyIndex > atomLength) {
                // TODO: find another exception
                throw new RuntimeException("The key index does not respect the arity of the atom " + atom);
            }
            else {
                listBuilder.add(atom.getTerm(keyIndex - 1));
            }
        }
        return listBuilder.build();
    }

    private static ImmutableSubstitution<VariableOrGroundTerm> unifyRedundantNodes(ImmutableCollection<DataNode> redundantNodes) {
        throw new RuntimeException("TODO: implement it");
    }

    private ImmutableSubstitution<VariableOrGroundTerm> mergeSubstitutions(ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> substitutions,
                                                                           ImmutableSet<Variable> variablesToKeep)
            throws UnsupportedUnificationException {
        throw new RuntimeException("TODO: implement it");
    }

    private static ImmutableCollection<DataNode> renameDataAtoms(ImmutableList<DataNode> dataNodes,
                                                                 ImmutableSubstitution<VariableOrGroundTerm> substitution) {
        throw new RuntimeException("TODO: implement it");
    }

    private ConcreteProposal createConcreteProposal(ImmutableList<PredicateLevelProposal> predicateProposals, ImmutableSet<Variable> variablesToKeep) {
        throw new RuntimeException("TODO: implement it");
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

    /**
     * Assumes that the data atoms are leafs
     */
    private static void applyOptimization(QueryTreeComponent treeComponent, InnerJoinNode topJoinNode,
                                          ConcreteProposal proposal) {
        for (DataNode nodeToRemove : proposal.getDataNodesToRemove()) {
            treeComponent.removeSubTree(nodeToRemove);
        }
        throw new RuntimeException("TODO: continue");
/*        for (DataNode newNode : proposal.getNewNodes()) {
            try {
                treeComponent.addChild(topJoinNode, newNode, Optional.<ArgumentPosition>absent(), false);
            } catch (IllegalTreeUpdateException e) {
                throw new RuntimeException("Unexpected: " + e.getMessage());
            }
        }*/
    }
}
