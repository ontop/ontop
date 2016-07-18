package it.unibz.inf.ontop.executor.join;

import java.util.Optional;
import com.google.common.collect.*;
import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableUnificationTools;
import it.unibz.inf.ontop.pivotalrepr.impl.ExtensionalDataNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.SubstitutionPropagationProposalImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;

/**
 * TODO: explain
 *
 * Assumption: clean inner join structure (an inner join does not have another inner join or filter node as a child).
 *
 * Naturally assumes that the data atoms are leafs.
 *
 */
public class RedundantSelfJoinExecutor implements SimpleNodeCentricInternalExecutor<InnerJoinNode, InnerJoinOptimizationProposal> {

    /**
     * TODO: explain
     */
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
    }


    /**
     * TODO: explain
     */
    protected static class ConcreteProposal {

        private final ImmutableCollection<DataNode> newDataNodes;
        private final Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution;
        private final ImmutableCollection<DataNode> removedDataNodes;

        protected ConcreteProposal(ImmutableCollection<DataNode> newDataNodes,
                                   Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution,
                                   ImmutableCollection<DataNode> removedDataNodes) {
            this.newDataNodes = newDataNodes;
            this.optionalSubstitution = optionalSubstitution;
            this.removedDataNodes = removedDataNodes;
        }

        public ImmutableCollection<DataNode> getNewDataNodes() {
            return newDataNodes;
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
     * TODO: explain
     *
     * TODO: find valid cases
     */
    private static final class AtomUnificationException extends Exception {
    }

    @Override
    public NodeCentricOptimizationResults<InnerJoinNode> apply(final InnerJoinOptimizationProposal highLevelProposal,
                                                final IntermediateQuery query,
                                                final QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {

        InnerJoinNode topJoinNode = highLevelProposal.getFocusNode();

        ImmutableMultimap<AtomPredicate, DataNode> initialMap = extractDataNodes(query.getChildren(topJoinNode));

        /**
         * Tries to optimize if there are data nodes
         */
        if (!initialMap.isEmpty()) {

            // TODO: explain
            ImmutableSet<Variable> variablesToKeep = query.getClosestConstructionNode(topJoinNode).getVariables();

            Optional<ConcreteProposal> optionalConcreteProposal = propose(initialMap, variablesToKeep,
                    query.getMetadata().getPrimaryKeys());

            if (optionalConcreteProposal.isPresent()) {
                ConcreteProposal concreteProposal = optionalConcreteProposal.get();

                // SIDE-EFFECT on the tree component (and thus on the query)
                return applyOptimization(query, treeComponent, highLevelProposal.getFocusNode(),
                        concreteProposal);
            }
        }
        // No optimization
        return new NodeCentricOptimizationResultsImpl<>(query, topJoinNode);
    }

    private Optional<ConcreteProposal> propose(ImmutableMultimap<AtomPredicate, DataNode> initialDataNodeMap,
                                               ImmutableSet<Variable> variablesToKeep,
                                               ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> primaryKeys) {

        ImmutableList.Builder<PredicateLevelProposal> proposalListBuilder = ImmutableList.builder();

        for (AtomPredicate predicate : initialDataNodeMap.keySet()) {
            ImmutableCollection<DataNode> initialNodes = initialDataNodeMap.get(predicate);
            PredicateLevelProposal predicateProposal;
            if (primaryKeys.containsKey(predicate)) {
                try {
                    predicateProposal = proposePerPredicate(initialNodes, primaryKeys.get(predicate));
                }
                /**
                 * Fall back to the default predicate proposal: doing nothing
                 */
                catch (AtomUnificationException e) {
                    predicateProposal = new PredicateLevelProposal(initialNodes);
                }
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
    private PredicateLevelProposal proposePerPredicate(ImmutableCollection<DataNode> dataNodes,
                                                       ImmutableCollection<ImmutableList<Integer>> primaryKeyPositions) throws AtomUnificationException {
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
                    break;
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
            ImmutableCollection<DataNode> dataNodes, ImmutableCollection<ImmutableList<Integer>> collectionOfPrimaryKeyPositions) {
        ImmutableMultimap.Builder<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMapBuilder = ImmutableMultimap.builder();

        for (ImmutableList<Integer> primaryKeyPositions : collectionOfPrimaryKeyPositions) {
            for (DataNode dataNode : dataNodes) {
                groupingMapBuilder.put(extractPrimaryKeyArguments(dataNode.getProjectionAtom(), primaryKeyPositions), dataNode);
            }
        }
        return groupingMapBuilder.build();
    }

    /**
     * TODO: explain
     */
    private static ImmutableList<VariableOrGroundTerm> extractPrimaryKeyArguments(DataAtom atom,
                                                                                  ImmutableList<Integer> primaryKeyPositions) {
        ImmutableList.Builder<VariableOrGroundTerm> listBuilder = ImmutableList.builder();
        int atomLength = atom.getArguments().size();

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

    private Optional<ConcreteProposal> createConcreteProposal(ImmutableList<PredicateLevelProposal> predicateProposals,
                                                              ImmutableSet<Variable> variablesToKeep) {
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalMergedSubstitution;
        try {
            optionalMergedSubstitution = mergeSubstitutions(predicateProposals, variablesToKeep);
        } catch (AtomUnificationException e) {
            return Optional.empty();
        }

        ImmutableSet.Builder<DataNode> removedDataNodeBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<DataNode> newDataNodeBuilder = ImmutableSet.builder();

        for (PredicateLevelProposal predicateProposal : predicateProposals) {
            if (optionalMergedSubstitution.isPresent()) {
                ImmutableSubstitution<VariableOrGroundTerm> mergedSubstitution = optionalMergedSubstitution.get();

                for (DataNode keptDataNode : predicateProposal.getKeptDataNodes()) {
                    DataAtom previousDataAtom = keptDataNode.getProjectionAtom();
                    DataAtom newDataAtom = mergedSubstitution.applyToDataAtom(previousDataAtom);

                    /**
                     * If new atom
                     */
                    if (!previousDataAtom.equals(newDataAtom)) {
                        newDataNodeBuilder.add(createNewDataNode(keptDataNode, newDataAtom));
                        removedDataNodeBuilder.add(keptDataNode);
                    }
                    /**
                     * Otherwise, no need to updated the "previous" data node
                     * (we keep it).
                     */
                }
            }
            removedDataNodeBuilder.addAll(predicateProposal.getRemovedDataNodes());
        }


        return Optional.of(new ConcreteProposal(newDataNodeBuilder.build(), optionalMergedSubstitution,
                removedDataNodeBuilder.build()));
    }

    private Optional<ImmutableSubstitution<VariableOrGroundTerm>> mergeSubstitutions(
            ImmutableList<PredicateLevelProposal> predicateProposals, ImmutableSet<Variable> variablesToTryToKeep)
            throws AtomUnificationException {
        ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> substitutions = extractSubstitutions(predicateProposals);

        // Non-final
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalAccumulatedSubstitution = Optional.empty();

        for (ImmutableSubstitution<VariableOrGroundTerm> substitution : substitutions) {
            if (!substitution.isEmpty()) {
                if (optionalAccumulatedSubstitution.isPresent()) {
                    Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalMGUS = ImmutableUnificationTools.computeAtomMGUS(
                            optionalAccumulatedSubstitution.get(), substitution);
                    if (optionalMGUS.isPresent()) {
                        optionalAccumulatedSubstitution = optionalMGUS;
                    }
                    else {
                        // TODO: log a warning
                        throw new AtomUnificationException();
                    }
                }
                else {
                    optionalAccumulatedSubstitution = Optional.of(substitution);
                }
            }
        }

        if (optionalAccumulatedSubstitution.isPresent()) {
            return Optional.of(optionalAccumulatedSubstitution.get().orientate(variablesToTryToKeep));
        }
        return optionalAccumulatedSubstitution;
    }

    private ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> extractSubstitutions(
            ImmutableCollection<PredicateLevelProposal> predicateProposals) {
        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> substitutionListBuilder = ImmutableList.builder();
        for (PredicateLevelProposal proposal : predicateProposals) {
            substitutionListBuilder.addAll(proposal.getSubstitutions());
        }
        return substitutionListBuilder.build();
    }


    private static ImmutableSubstitution<VariableOrGroundTerm> unifyRedundantNodes(
            ImmutableCollection<DataNode> redundantNodes) throws AtomUnificationException {
        // Non-final
        ImmutableSubstitution<VariableOrGroundTerm> accumulatedSubstitution = new ImmutableSubstitutionImpl<>(
                ImmutableMap.<Variable, VariableOrGroundTerm>of());

        /**
         * Should normally not be called in this case.
         */
        if (redundantNodes.size() < 2) {
            // Empty substitution
            return accumulatedSubstitution;
        }

        UnmodifiableIterator<DataNode> nodeIterator = redundantNodes.iterator();

        // Non-final
        DataAtom accumulatedAtom = nodeIterator.next().getProjectionAtom();
        while (nodeIterator.hasNext()) {
            DataAtom newAtom = nodeIterator.next().getProjectionAtom();
            // May throw an exception
            accumulatedSubstitution = updateSubstitution(accumulatedSubstitution, accumulatedAtom, newAtom);
            accumulatedAtom = accumulatedSubstitution.applyToDataAtom(accumulatedAtom);
        }
        return accumulatedSubstitution;
    }

    /**
     * TODO: explain
     */
    private static ImmutableSubstitution<VariableOrGroundTerm> updateSubstitution(
            final ImmutableSubstitution<VariableOrGroundTerm> accumulatedSubstitution,
            final DataAtom accumulatedAtom,  final DataAtom newAtom) throws AtomUnificationException {
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution =
                ImmutableUnificationTools.computeAtomMGU(accumulatedAtom, newAtom);

        if (optionalSubstitution.isPresent()) {
            if (accumulatedSubstitution.isEmpty()) {
                return optionalSubstitution.get();
            }
            else {
                Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalAccumulatedSubstitution =
                        ImmutableUnificationTools.computeAtomMGUS(accumulatedSubstitution, optionalSubstitution.get());
                if (optionalAccumulatedSubstitution.isPresent()) {
                    return optionalAccumulatedSubstitution.get();
                }
                /**
                 * Cannot unify the two substitutions
                 */
                else {
                    // TODO: log a warning
                    throw new AtomUnificationException();
                }
            }
        }
        /**
         * Cannot unify the two atoms
         */
        else {
            // TODO: log a warning
            throw new AtomUnificationException();
        }
    }


    private static ImmutableMultimap<AtomPredicate, DataNode> extractDataNodes(ImmutableList<QueryNode> siblings) {
        ImmutableMultimap.Builder<AtomPredicate, DataNode> mapBuilder = ImmutableMultimap.builder();
        for (QueryNode node : siblings) {
            if (node instanceof DataNode) {
                DataNode dataNode = (DataNode) node;
                mapBuilder.put(dataNode.getProjectionAtom().getPredicate(), dataNode);
            }
        }
        return mapBuilder.build();
    }

    private DataNode createNewDataNode(DataNode previousDataNode, DataAtom newDataAtom) {
        if (previousDataNode instanceof ExtensionalDataNode) {
            return new ExtensionalDataNodeImpl(newDataAtom);
        }
        else {
            throw new IllegalArgumentException("Unexpected type of data node: " + previousDataNode);
        }
    }

    /**
     * Assumes that the data atoms are leafs.
     *
     *
     *
     */
    private static NodeCentricOptimizationResults<InnerJoinNode> applyOptimization(IntermediateQuery query,
                                                                                   QueryTreeComponent treeComponent,
                                                                                   InnerJoinNode topJoinNode,
                                                                                   ConcreteProposal proposal) {
        /**
         * First, add and remove non-top nodes
         */
        proposal.getDataNodesToRemove()
                .forEach(treeComponent::removeSubTree);

        proposal.getNewDataNodes()
                .forEach(newNode -> treeComponent.addChild(topJoinNode, newNode,
                        Optional.<NonCommutativeOperatorNode.ArgumentPosition>empty(), false));

        switch(treeComponent.getChildren(topJoinNode).size()) {
            case 0:
                throw new IllegalStateException("Self-join elimination MUST not eliminate ALL the nodes");

                /**
                 * Special case: only one child remains so the join node is not needed anymore.
                 *
                 * Creates a FILTER node if there is a joining condition
                 */
            case 1:
                QueryNode uniqueChild = treeComponent.getFirstChild(topJoinNode).get();
                Optional<ImmutableExpression> optionalFilter = topJoinNode.getOptionalFilterCondition();

                QueryNode newTopNode;
                if (optionalFilter.isPresent()) {
                    newTopNode = new FilterNodeImpl(optionalFilter.get());
                    treeComponent.replaceNode(topJoinNode, newTopNode);
                }
                else {
                    treeComponent.removeOrReplaceNodeByUniqueChildren(topJoinNode);
                    newTopNode = uniqueChild;
                }

                QueryNode updatedTopNode = propagateSubstitution(query, proposal.getOptionalSubstitution(), newTopNode);
                return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(updatedTopNode));

            /**
             * Multiple children, keep the top join node
             */
            default:
                InnerJoinNode updatedTopJoinNode = propagateSubstitution(query, proposal.getOptionalSubstitution(),
                        topJoinNode);
                return new NodeCentricOptimizationResultsImpl<>(query, updatedTopJoinNode);
        }
    }

    /**
     *  Applies the substitution from the topNode
     */
    private static <N extends QueryNode> N propagateSubstitution(IntermediateQuery query,
                                                                 Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution,
                                                                 N topNode) {
        if (optionalSubstitution.isPresent()) {

            // TODO: filter the non-bound variables from the substitution before propagating!!!

            SubstitutionPropagationProposal<N> propagationProposal = new SubstitutionPropagationProposalImpl<>(
                    topNode, optionalSubstitution.get());

            // Forces the use of an internal executor (the treeComponent must remain the same).
            try {
                NodeCentricOptimizationResults<N> results = query.applyProposal(propagationProposal, true);

                return results.getOptionalNewNode()
                        .orElseThrow(() -> new IllegalStateException(
                                "No focus node returned after the substitution propagation"));

            } catch (EmptyQueryException e) {
                throw new IllegalStateException("Internal inconsistency error: propagation the substitution " +
                        "leads to an empty query: " + optionalSubstitution.get());
            }
        }
        /**
         * No substitution to propagate
         */
        else {
            return topNode;
        }
    }
}
