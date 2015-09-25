package org.semanticweb.ontop.executor.join;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import org.semanticweb.ontop.executor.NodeCentricInternalExecutor;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableUnificationTools;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.impl.ExtensionalDataNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.VariableCollector;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;

/**
 * TODO: explain
 *
 * Assumption: clean inner join structure (an inner join does not have another inner join or filter node as a child).
 *
 * Naturally assumes that the data atoms are leafs.
 *
 */
public class RedundantSelfJoinExecutor implements NodeCentricInternalExecutor<InnerJoinNode, InnerJoinOptimizationProposal> {

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
            ImmutableSet<Variable> variablesToKeep = VariableCollector.collectVariables(
                    query.getClosestConstructionNode(topJoinNode));

            Optional<ConcreteProposal> optionalConcreteProposal = propose(initialMap, variablesToKeep,
                    query.getMetadata().getPrimaryKeys());

            if (optionalConcreteProposal.isPresent()) {
                ConcreteProposal concreteProposal = optionalConcreteProposal.get();

                // SIDE-EFFECT on the tree component (and thus on the query)
                applyOptimization(treeComponent, highLevelProposal.getFocusNode(), concreteProposal);
            }
        }
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
                groupingMapBuilder.put(extractPrimaryKeyArguments(dataNode.getAtom(), primaryKeyPositions), dataNode);
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

    private Optional<ConcreteProposal> createConcreteProposal(ImmutableList<PredicateLevelProposal> predicateProposals,
                                                              ImmutableSet<Variable> variablesToKeep) {
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalMergedSubstitution;
        try {
            optionalMergedSubstitution = mergeSubstitutions(predicateProposals, variablesToKeep);
        } catch (AtomUnificationException e) {
            return Optional.absent();
        }

        ImmutableSet.Builder<DataNode> removedDataNodeBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<DataNode> newDataNodeBuilder = ImmutableSet.builder();

        for (PredicateLevelProposal predicateProposal : predicateProposals) {
            if (optionalMergedSubstitution.isPresent()) {
                ImmutableSubstitution<VariableOrGroundTerm> mergedSubstitution = optionalMergedSubstitution.get();

                for (DataNode keptDataNode : predicateProposal.getKeptDataNodes()) {
                    DataAtom previousDataAtom = keptDataNode.getAtom();
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
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalAccumulatedSubstitution = Optional.absent();

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
        DataAtom accumulatedAtom = nodeIterator.next().getAtom();
        while (nodeIterator.hasNext()) {
            DataAtom newAtom = nodeIterator.next().getAtom();
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
                mapBuilder.put(dataNode.getAtom().getPredicate(), dataNode);
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
     * Assumes that the data atoms are leafs
     */
    private static void applyOptimization(QueryTreeComponent treeComponent, InnerJoinNode topJoinNode,
                                          ConcreteProposal proposal) {
        for (DataNode nodeToRemove : proposal.getDataNodesToRemove()) {
            treeComponent.removeSubTree(nodeToRemove);
        }
        for (DataNode newNode : proposal.getNewDataNodes()) {
            try {
                treeComponent.addChild(topJoinNode, newNode, Optional.<ArgumentPosition>absent(), false);
            } catch (IllegalTreeUpdateException e) {
                throw new RuntimeException("Unexpected: " + e.getMessage());
            }
        }

        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution = proposal.getOptionalSubstitution();
        if (optionalSubstitution.isPresent()) {
            /**
             * TODO: propagate the substitution
             */
            throw new RuntimeException("TODO: propagate the substitution to the upper construction node " +
                    "and the surroundings");
        }
    }
}
