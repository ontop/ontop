package it.unibz.inf.ontop.executor.join;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableUnificationTools;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.impl.ExtensionalDataNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.SubstitutionPropagationProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;

public class SelfJoinLikeExecutor {

    /**
     * TODO: explain
     *
     * TODO: find valid cases
     */
    protected static final class AtomUnificationException extends Exception {
    }

    /**
     * Unchecked temporary variant of AtomUnificationException, so that the functional style can still be used.
     */
    private static final class AtomUnificationRuntimeException extends RuntimeException {
        public final AtomUnificationException checkedException;

        public AtomUnificationRuntimeException(AtomUnificationException e) {
            this.checkedException = e;
        }
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

            if (keptDataNodes.stream()
                    .anyMatch(removedDataNodes::contains)) {
                throw new IllegalStateException("A node cannot be kept and removed at the same time");
            }
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

        public ConcreteProposal(ImmutableCollection<DataNode> newDataNodes,
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
     * Mutable object (for efficiency)
     */
    private static class Dominance {

        private final List<DataNode> locallyDominants;
        private final Set<DataNode> removalNodes;

        public Dominance() {
            this(Lists.newArrayList(), Sets.newHashSet());
        }

        private Dominance(List<DataNode> locallyDominants, Set<DataNode> removalNodes) {
            this.locallyDominants = locallyDominants;
            this.removalNodes = removalNodes;
        }

        public Dominance update(Collection<DataNode> sameRowDataNodes) {
            DataNode locallyDominantNode = sameRowDataNodes.stream()
                    .filter(locallyDominants::contains)
                    .findFirst()
                    .orElseGet(() -> sameRowDataNodes.stream()
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("Should be at least one node")));
            locallyDominants.add(locallyDominantNode);

            /**
             * Adds all the non-dominants to the removal set.
             */
            sameRowDataNodes.stream()
                    .filter(n -> n != locallyDominantNode)
                    .forEach(removalNodes::add);

            return this;
        }

        private final ImmutableSet<DataNode> getRemovalNodes() {
            return ImmutableSet.copyOf(removalNodes);
        }
    }

    private static class ParentAndChildPosition {
        public final QueryNode parent;
        public final Optional<ArgumentPosition> position;

        private ParentAndChildPosition(QueryNode parent, Optional<ArgumentPosition> position) {
            this.parent = parent;
            this.position = position;
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
     * groupingMap groups data nodes that are being joined on the unique constraints
     *
     * creates proposal to unify redundant nodes
     */
    protected static PredicateLevelProposal proposeForGroupingMap(
            ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMap)
    throws AtomUnificationException {

        ImmutableCollection<Collection<DataNode>> dataNodeGroups = groupingMap.asMap().values();

        try {
            /**
             * Collection of unifying substitutions
             */
            ImmutableSet<ImmutableSubstitution<VariableOrGroundTerm>> unifyingSubstitutions =
                    dataNodeGroups.stream()
                            .filter(g -> g.size() > 1)
                            .map(redundantNodes -> {
                                    try {
                                        return unifyRedundantNodes(redundantNodes);
                                    } catch (AtomUnificationException e) {
                                        throw new AtomUnificationRuntimeException(e);
                                    }
                            })
                            .filter(s -> !s.isEmpty())
                            .collect(ImmutableCollectors.toSet());
            /**
             * All the nodes that have been at least once dominated (--> could thus be removed).
             *
             * Not parallellizable
             */
            ImmutableSet<DataNode> removableNodes = ImmutableSet.copyOf(dataNodeGroups.stream()
                    .filter(sameRowDataNodes -> sameRowDataNodes.size() > 1)
                    .reduce(
                            new Dominance(),
                            Dominance::update,
                            (dom1, dom2) -> {
                                throw new IllegalStateException("Cannot be run in parallel");
                            })
                    .getRemovalNodes());

            ImmutableSet<DataNode> keptNodes = dataNodeGroups.stream()
                    .flatMap(Collection::stream)
                    .filter(n -> !removableNodes.contains(n))
                    .collect(ImmutableCollectors.toSet());

            return new PredicateLevelProposal(keptNodes, unifyingSubstitutions, removableNodes);

            /**
             * Trick: rethrow the exception
              */
        } catch (AtomUnificationRuntimeException e) {
            throw e.checkedException;
        }
    }

    protected static Optional<ConcreteProposal> createConcreteProposal(
            ImmutableList<PredicateLevelProposal> predicateProposals,
            ImmutableList<Variable> priorityVariables) {
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalMergedSubstitution;
        try {
            optionalMergedSubstitution = mergeSubstitutions(predicateProposals, priorityVariables);
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

        ImmutableSet<DataNode> newDataNodes = newDataNodeBuilder.build();
        ImmutableSet<DataNode> removedDataNodes = removedDataNodeBuilder.build();

        if (newDataNodes.isEmpty()
                && removedDataNodes.isEmpty()
                && (! optionalMergedSubstitution.isPresent()))
            return Optional.empty();

        return Optional.of(new ConcreteProposal(newDataNodes, optionalMergedSubstitution, removedDataNodes));
    }

    private static DataNode createNewDataNode(DataNode previousDataNode, DataAtom newDataAtom) {
        if (previousDataNode instanceof ExtensionalDataNode) {
            return previousDataNode.newAtom(newDataAtom);
        }
        else {
            throw new IllegalArgumentException("Unexpected type of data node: " + previousDataNode);
        }
    }


    protected static ImmutableSubstitution<VariableOrGroundTerm> unifyRedundantNodes(
            Collection<DataNode> redundantNodes) throws AtomUnificationException {
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

        Iterator<DataNode> nodeIterator = redundantNodes.iterator();

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
            ImmutableList<PredicateLevelProposal> predicateProposals, ImmutableList<Variable> priorityVariables)
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

        return optionalAccumulatedSubstitution
                .map(s -> s.orientate(priorityVariables));
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
            ConcreteProposal proposal) throws EmptyQueryException {
        switch(treeComponent.getChildren(joinNode).size()) {
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
                    newTopNode = query.getFactory().createFilterNode(optionalFilter.get());
                    treeComponent.replaceNode(joinNode, newTopNode);
                }
                else {
                    treeComponent.removeOrReplaceNodeByUniqueChild(joinNode);
                    newTopNode = uniqueChild;
                }

                NodeCentricOptimizationResults<QueryNode> propagationResults = propagateSubstitution(query,
                        proposal.getOptionalSubstitution(), newTopNode);
                /**
                 * Converts it into NodeCentricOptimizationResults over the focus node
                 */
                return propagationResults.getNewNodeOrReplacingChild()
                        // Replaces a descendant
                        .map(child -> new NodeCentricOptimizationResultsImpl<N>(query, Optional.of(child)))
                        // The sub-tree of the join is removed
                        .orElseGet(() -> new NodeCentricOptimizationResultsImpl<N>(query,
                                propagationResults.getOptionalNextSibling(),
                                propagationResults.getOptionalClosestAncestor()));
            /**
             * Multiple children, keep the top join node
             */
            default:
                return propagateSubstitution(query, proposal.getOptionalSubstitution(), joinNode);
        }
    }

    /**
     *  Applies the substitution from the topNode.
     *
     *  NB: the topNode can be a JoinLikeNode, a replacing FilterNode or the replacing child
     */
    private static <T extends QueryNode> NodeCentricOptimizationResults<T> propagateSubstitution(
            IntermediateQuery query,
            Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution,
            T topNode) throws EmptyQueryException {
        if (optionalSubstitution.isPresent()) {

            // TODO: filter the non-bound variables from the substitution before propagating!!!

            SubstitutionPropagationProposal<T> propagationProposal = new SubstitutionPropagationProposalImpl<>(
                    topNode, optionalSubstitution.get(), false);

            // Forces the use of an internal executor (the treeComponent must remain the same).
            return query.applyProposal(propagationProposal);
        }
        /**
         * No substitution to propagate
         */
        else {
            return new NodeCentricOptimizationResultsImpl<>(query, topNode);
        }

    }

    /**
     * TODO: implement seriously
     */
    protected ImmutableList<Variable> prioritizeVariables(IntermediateQuery query, JoinLikeNode joinLikeNode) {

        /**
         * Sequential (order matters)
         */
        return extractAncestors(query, joinLikeNode).stream()
                .sequential()
                .flatMap(p -> extractPriorityVariables(query, p.parent, p.position))
                .distinct()
                .collect(ImmutableCollectors.toList());
    }

    private ImmutableList<ParentAndChildPosition> extractAncestors(IntermediateQuery query, QueryNode node) {
        // Non-final
        Optional<QueryNode> optionalAncestor = query.getParent(node);
        QueryNode ancestorChild = node;

        ImmutableList.Builder<ParentAndChildPosition> listBuilder = ImmutableList.builder();

        while (optionalAncestor.isPresent()) {
            QueryNode ancestor = optionalAncestor.get();
            listBuilder.add(new ParentAndChildPosition(ancestor, query.getOptionalPosition(ancestor, ancestorChild)));

            optionalAncestor = query.getParent(ancestor);
            ancestorChild = ancestor;
        }

        return listBuilder.build();
    }


    private Stream<Variable> extractPriorityVariables(IntermediateQuery query, QueryNode node,
                                                      Optional<ArgumentPosition> childPosition) {
        if (node instanceof ExplicitVariableProjectionNode)
            return ((ExplicitVariableProjectionNode)node).getVariables().stream();

        /**
         * LJ: look for variables on the left
         *     only when the focus node is on the right
         */
        else if (node instanceof LeftJoinNode) {
            switch (childPosition.get()) {
                case RIGHT:
                    return query.getChild(node, LEFT)
                            .map(c -> query.getVariables(c).stream())
                            .orElseThrow(() -> new IllegalStateException("A LJ must have a left child"));
                case LEFT:
                default:
                    return Stream.empty();
            }
        }
        else {
            return Stream.empty();
        }
    }

}
