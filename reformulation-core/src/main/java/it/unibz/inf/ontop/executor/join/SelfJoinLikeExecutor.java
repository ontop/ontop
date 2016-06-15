package it.unibz.inf.ontop.executor.join;

import com.google.common.collect.*;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableUnificationTools;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.ExtensionalDataNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.SubstitutionPropagationProposalImpl;

import java.util.Optional;

public class SelfJoinLikeExecutor {

    /**
     * TODO: explain
     *
     * TODO: find valid cases
     */
    protected static final class AtomUnificationException extends Exception {
    }


    /**
     * TODO: explain
     */
    protected static class PredicateLevelProposal {

        private final ImmutableCollection<DataNode> keptDataNodes;
        private final ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> substitutions;
        private final ImmutableCollection<DataNode> removedDataNodes;

        public PredicateLevelProposal(ImmutableCollection<DataNode> keptDataNodes,
                                         ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> substitutions,
                                         ImmutableCollection<DataNode> removedDataNodes) {
            this.keptDataNodes = keptDataNodes;
            this.substitutions = substitutions;
            this.removedDataNodes = removedDataNodes;
        }

        /**
         * No redundancy
         */
        public PredicateLevelProposal(ImmutableCollection<DataNode> initialDataNodes) {
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


    protected static ImmutableMultimap<AtomPredicate, DataNode> extractDataNodes(ImmutableList<QueryNode> siblings) {
        ImmutableMultimap.Builder<AtomPredicate, DataNode> mapBuilder = ImmutableMultimap.builder();
        for (QueryNode node : siblings) {
            if (node instanceof DataNode) {
                DataNode dataNode = (DataNode) node;
                mapBuilder.put(dataNode.getProjectionAtom().getPredicate(), dataNode);
            }
        }
        return mapBuilder.build();
    }

    /**
     * groupingMap groups data nodes that are being joined on the primary keys
     *
     * creates proposal to unify redundant nodes
     */
    protected static PredicateLevelProposal proposeForGroupingMap(
            ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMap)
    throws AtomUnificationException {
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

    protected static Optional<ConcreteProposal> createConcreteProposal(
            ImmutableList<PredicateLevelProposal> predicateProposals,
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

    private static DataNode createNewDataNode(DataNode previousDataNode, DataAtom newDataAtom) {
        if (previousDataNode instanceof ExtensionalDataNode) {
            return new ExtensionalDataNodeImpl(newDataAtom);
        }
        else {
            throw new IllegalArgumentException("Unexpected type of data node: " + previousDataNode);
        }
    }


    protected static ImmutableSubstitution<VariableOrGroundTerm> unifyRedundantNodes(
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

    protected static Optional<ImmutableSubstitution<VariableOrGroundTerm>> mergeSubstitutions(
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

    protected static ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> extractSubstitutions(
            ImmutableCollection<PredicateLevelProposal> predicateProposals) {
        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> substitutionListBuilder = ImmutableList.builder();
        for (PredicateLevelProposal proposal : predicateProposals) {
            substitutionListBuilder.addAll(proposal.getSubstitutions());
        }
        return substitutionListBuilder.build();
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


    /**
     * Returns the list of the terms from atom corresponding
     * to the positions
     * TODO: explain
     */
    protected static ImmutableList<VariableOrGroundTerm> extractPrimaryKeyArguments(DataAtom atom,
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

    protected static <N extends JoinOrFilterNode> NodeCentricOptimizationResults<N> getJoinNodeCentricOptimizationResults(
            IntermediateQuery query,
            QueryTreeComponent treeComponent,
            N joinNode,
            ConcreteProposal proposal) {
        switch(treeComponent.getCurrentSubNodesOf(joinNode).size()) {
            case 0:
                throw new IllegalStateException("Self-join elimination MUST not eliminate ALL the nodes");

                /**
                 * Special case: only one child remains so the join node is not needed anymore.
                 *
                 * Creates a FILTER node if there is a joining condition
                 */
            case 1:
                QueryNode uniqueChild = treeComponent.getFirstChild(joinNode).get();
                Optional<ImmutableExpression> optionalFilter = joinNode.getOptionalFilterCondition();

                QueryNode newTopNode;
                if (optionalFilter.isPresent()) {
                    newTopNode = new FilterNodeImpl(optionalFilter.get());
                    treeComponent.replaceNode(joinNode, newTopNode);
                }
                else {
                    treeComponent.removeOrReplaceNodeByUniqueChildren(joinNode);
                    newTopNode = uniqueChild;
                }

                QueryNode updatedTopNode = propagateSubstitution(query, proposal.getOptionalSubstitution(), newTopNode);
                return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(updatedTopNode));

            /**
             * Multiple children, keep the top join node
             */
            default:
                N updatedLeftJoinNode = propagateSubstitution(
                        query, proposal.getOptionalSubstitution(), joinNode);
                return new NodeCentricOptimizationResultsImpl<>(query, updatedLeftJoinNode);
        }
    }

    /**
     *  Applies the substitution from the topNode
     */
    private static <N extends QueryNode> N propagateSubstitution(
            IntermediateQuery query,
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
