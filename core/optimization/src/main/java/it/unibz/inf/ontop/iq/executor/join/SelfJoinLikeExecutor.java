package it.unibz.inf.ontop.iq.executor.join;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.iq.proposal.impl.SubstitutionPropagationProposalImpl;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;

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

        private final ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> substitutions;
        private final ImmutableCollection<ExtensionalDataNode> removedDataNodes;

        public PredicateLevelProposal(ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> substitutions,
                                      ImmutableCollection<ExtensionalDataNode> removedDataNodes) {
            this.substitutions = substitutions;
            this.removedDataNodes = removedDataNodes;
        }

        public ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> getSubstitutions() {
            return substitutions;
        }

        /**
         * Not unified
         */
        public ImmutableCollection<ExtensionalDataNode> getRemovedDataNodes() {
            return removedDataNodes;
        }
    }


    /**
     * TODO: explain
     */
    protected static class ConcreteProposal {

        private final Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution;
        private final ImmutableCollection<DataNode> removedDataNodes;

        public ConcreteProposal(Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution,
                                ImmutableCollection<DataNode> removedDataNodes) {
            this.optionalSubstitution = optionalSubstitution;
            this.removedDataNodes = removedDataNodes;
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

        private final List<ExtensionalDataNode> locallyDominants;
        private final Set<ExtensionalDataNode> removalNodes;

        public Dominance() {
            this(Lists.newArrayList(), Sets.newHashSet());
        }

        private Dominance(List<ExtensionalDataNode> locallyDominants, Set<ExtensionalDataNode> removalNodes) {
            this.locallyDominants = locallyDominants;
            this.removalNodes = removalNodes;
        }

        public Dominance update(Collection<ExtensionalDataNode> sameRowDataNodes) {
            ExtensionalDataNode locallyDominantNode = sameRowDataNodes.stream()
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

        private final ImmutableSet<ExtensionalDataNode> getRemovalNodes() {
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


    private final SubstitutionFactory substitutionFactory;
    private final ImmutableUnificationTools unificationTools;

    protected SelfJoinLikeExecutor(SubstitutionFactory substitutionFactory, ImmutableUnificationTools unificationTools) {
        this.substitutionFactory = substitutionFactory;
        this.unificationTools = unificationTools;
    }


    protected static ImmutableMultimap<RelationPredicate, ExtensionalDataNode> extractDataNodes(ImmutableList<QueryNode> siblings) {
        ImmutableMultimap.Builder<RelationPredicate, ExtensionalDataNode> mapBuilder = ImmutableMultimap.builder();
        for (QueryNode node : siblings) {
            if (node instanceof ExtensionalDataNode) {
                ExtensionalDataNode dataNode = (ExtensionalDataNode) node;
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
    protected PredicateLevelProposal proposeForGroupingMap(
            ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, ExtensionalDataNode> groupingMap)
    throws AtomUnificationException {

        ImmutableCollection<Collection<ExtensionalDataNode>> dataNodeGroups = groupingMap.asMap().values();

        try {
            /*
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
            /*
             * All the nodes that have been at least once dominated (--> could thus be removed).
             *
             * Not parallellizable
             */
            ImmutableSet<ExtensionalDataNode> removableNodes = ImmutableSet.copyOf(dataNodeGroups.stream()
                    .filter(sameRowDataNodes -> sameRowDataNodes.size() > 1)
                    .reduce(
                            new Dominance(),
                            Dominance::update,
                            (dom1, dom2) -> {
                                throw new IllegalStateException("Cannot be run in parallel");
                            })
                    .getRemovalNodes());

            return new PredicateLevelProposal(unifyingSubstitutions, removableNodes);

            /*
             * Trick: rethrow the exception
              */
        } catch (AtomUnificationRuntimeException e) {
            throw e.checkedException;
        }
    }

    protected Optional<ConcreteProposal> createConcreteProposal(
            ImmutableList<PredicateLevelProposal> predicateProposals,
            ImmutableList<Variable> priorityVariables) {
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalMergedSubstitution;
        try {
            optionalMergedSubstitution = mergeSubstitutions(extractSubstitutions(predicateProposals), priorityVariables);
        } catch (AtomUnificationException e) {
            return Optional.empty();
        }

        ImmutableSet<DataNode> removedDataNodes =predicateProposals.stream()
                .flatMap(p -> p.getRemovedDataNodes().stream())
                .collect(ImmutableCollectors.toSet());

        if (removedDataNodes.isEmpty()
                && (! optionalMergedSubstitution.isPresent()))
            return Optional.empty();

        return Optional.of(new ConcreteProposal(optionalMergedSubstitution, removedDataNodes));
    }


    protected ImmutableSubstitution<VariableOrGroundTerm> unifyRedundantNodes(
            Collection<ExtensionalDataNode> redundantNodes) throws AtomUnificationException {
        // Non-final
        ImmutableSubstitution<VariableOrGroundTerm> accumulatedSubstitution = substitutionFactory.getSubstitution();

        /*
         * Should normally not be called in this case.
         */
        if (redundantNodes.size() < 2) {
            // Empty substitution
            return accumulatedSubstitution;
        }

        Iterator<ExtensionalDataNode> nodeIterator = redundantNodes.iterator();

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

    protected Optional<ImmutableSubstitution<VariableOrGroundTerm>> mergeSubstitutions(
            ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> substitutions, ImmutableList<Variable> priorityVariables)
            throws AtomUnificationException {

        // Non-final
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalAccumulatedSubstitution = Optional.empty();

        for (ImmutableSubstitution<VariableOrGroundTerm> substitution : substitutions) {
            if (!substitution.isEmpty()) {
                if (optionalAccumulatedSubstitution.isPresent()) {
                    Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalMGUS = unificationTools.computeAtomMGUS(
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

    protected ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> extractSubstitutions(
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
    private ImmutableSubstitution<VariableOrGroundTerm> updateSubstitution(
            final ImmutableSubstitution<VariableOrGroundTerm> accumulatedSubstitution,
            final DataAtom accumulatedAtom,  final DataAtom newAtom) throws AtomUnificationException {
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution =
                unificationTools.computeAtomMGU(accumulatedAtom, newAtom);

        if (optionalSubstitution.isPresent()) {
            if (accumulatedSubstitution.isEmpty()) {
                return optionalSubstitution.get();
            }
            else {
                Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalAccumulatedSubstitution =
                        unificationTools.computeAtomMGUS(accumulatedSubstitution, optionalSubstitution.get());
                if (optionalAccumulatedSubstitution.isPresent()) {
                    return optionalAccumulatedSubstitution.get();
                }
                /*
                 * Cannot unify the two substitutions
                 */
                else {
                    // TODO: log a warning
                    throw new AtomUnificationException();
                }
            }
        }
        /*
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
    protected static ImmutableList<VariableOrGroundTerm> extractArguments(DataAtom atom,
                                                                          ImmutableList<Integer> positions) {
        ImmutableList.Builder<VariableOrGroundTerm> listBuilder = ImmutableList.builder();
        int atomLength = atom.getArguments().size();

        for (Integer keyIndex : positions) {
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

    protected <N extends JoinOrFilterNode> NodeCentricOptimizationResults<N> updateJoinNodeAndPropagateSubstitution(
            IntermediateQuery query,
            QueryTreeComponent treeComponent,
            N joinNode,
            ConcreteProposal proposal) throws EmptyQueryException {
        switch(treeComponent.getChildren(joinNode).size()) {
            case 0:
                throw new IllegalStateException("Self-join elimination MUST not eliminate ALL the nodes");

                /*
                 * Special case: only one child remains so the join node is not needed anymore.
                 *
                 * Creates a FILTER node if there is a joining condition
                 */
            case 1:
                QueryNode uniqueChild = treeComponent.getFirstChild(joinNode).get();
                Optional<ImmutableExpression> optionalFilter = joinNode.getOptionalFilterCondition();

                if (optionalFilter.isPresent()) {
                    QueryNode filterNode = query.getFactory().createFilterNode(optionalFilter.get());
                    treeComponent.replaceNode(joinNode, filterNode);
                }
                else {
                    treeComponent.removeOrReplaceNodeByUniqueChild(joinNode);
                }

                NodeCentricOptimizationResults<QueryNode> propagationResults = propagateSubstitution(query,
                        proposal.getOptionalSubstitution(), uniqueChild);
                /*
                 * Converts it into NodeCentricOptimizationResults over the focus node
                 */
                return propagationResults.getNewNodeOrReplacingChild()
                        // Replaces a descendant
                        .map(child -> new NodeCentricOptimizationResultsImpl<N>(query, Optional.of(child)))
                        // The sub-tree of the join is removed
                        .orElseGet(() -> new NodeCentricOptimizationResultsImpl<N>(query,
                                propagationResults.getOptionalNextSibling(),
                                propagationResults.getOptionalClosestAncestor()));
            /*
             * Multiple children, keep a join node BUT MOVES ITS CONDITION ABOVE (in a filter)
             */
            default:
                N newJoinNode = joinNode.getOptionalFilterCondition()
                        .map(cond -> {
                            FilterNode parentFilter = query.getFactory().createFilterNode(cond);
                            treeComponent.insertParent(joinNode, parentFilter);

                            N conditionLessJoinNode = (N) ((JoinLikeNode) joinNode)
                                    .changeOptionalFilterCondition(Optional.empty());
                            treeComponent.replaceNode(joinNode, conditionLessJoinNode);
                            return conditionLessJoinNode;
                        })
                        .orElse(joinNode);
                return propagateSubstitution(query, proposal.getOptionalSubstitution(), newJoinNode);
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
            return query.applyProposal(propagationProposal, true);
        }
        /*
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

        /*
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

        /*
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
