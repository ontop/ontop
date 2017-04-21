package it.unibz.inf.ontop.executor.join;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.RemoveEmptyNodeProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Assumption: clean inner join structure (an inner join does not have another inner join or filter node as a child).
 *
 * Naturally assumes that the data atoms are leafs.
 *
 */
public abstract class RedundantSelfJoinExecutor extends SelfJoinLikeExecutor implements InnerJoinExecutor {

    /**
     * Safety, to prevent infinite loops
     */
    private static final int MAX_ITERATIONS = 100;
    private final IntermediateQueryFactory iqFactory;

    protected RedundantSelfJoinExecutor(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }


    @Override
    public NodeCentricOptimizationResults<InnerJoinNode> apply(final InnerJoinOptimizationProposal highLevelProposal,
                                                final IntermediateQuery query,
                                                final QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        // Non-final
        InnerJoinNode topJoinNode = highLevelProposal.getFocusNode();

        ImmutableMultimap<AtomPredicate, DataNode> initialMap = extractDataNodes(query.getChildren(topJoinNode));

        /*
         * Tries to optimize if there are data nodes
         */
        int i=0;
        while (!initialMap.isEmpty() && (i++ < MAX_ITERATIONS)) {

            ImmutableList<Variable> priorityVariables = prioritizeVariables(query, topJoinNode);

            try {
                Optional<ConcreteProposal> optionalConcreteProposal = propose(initialMap, priorityVariables,
                        query.getDBMetadata());

                if (!optionalConcreteProposal.isPresent()) {
                    break;
                }
                else {
                    ConcreteProposal concreteProposal = optionalConcreteProposal.get();

                    // SIDE-EFFECT on the tree component (and thus on the query)
                    NodeCentricOptimizationResults<InnerJoinNode> result = applyOptimization(query, treeComponent,
                            topJoinNode, concreteProposal);

                    /*
                     * No change --> breaks the loop
                     */
                    if (result.getOptionalNewNode().isPresent()) {
                        int oldSize = initialMap.size();
                        initialMap = extractDataNodes(query.getChildren(
                                result.getOptionalNewNode().get()));
                        int newSize = initialMap.size();

                        if (oldSize == newSize) {
                            return result;
                        } else if (oldSize < newSize) {
                            throw new IllegalStateException("The number of data atoms was expected to decrease, not increase");
                        }
                        // else, continue
                        topJoinNode = result.getOptionalNewNode().get();

                    } else {
                        return result;
                    }
                }
                /*
                 * No unification --> empty result
                 */
            } catch (AtomUnificationException e) {
                return removeSubTree(query, treeComponent, topJoinNode);
            }
        }

        /*
         * Safety
         */
        if (i >= MAX_ITERATIONS) {
            throw new IllegalStateException("Redundant self-join elimination loop has reached " +
                    "the max iteration threshold (" + MAX_ITERATIONS + ")");
        }

        // No optimization
        return new NodeCentricOptimizationResultsImpl<>(query, topJoinNode);
    }

    /**
     * Throws an AtomUnificationException when the results are guaranteed to be empty
     */
    private Optional<ConcreteProposal> propose(ImmutableMultimap<AtomPredicate, DataNode> initialDataNodeMap,
                                               ImmutableList<Variable> priorityVariables,
                                               DBMetadata dbMetadata)
            throws AtomUnificationException {

        ImmutableList.Builder<PredicateLevelProposal> proposalListBuilder = ImmutableList.builder();

        for (AtomPredicate predicate : initialDataNodeMap.keySet()) {
            ImmutableCollection<DataNode> initialNodes = initialDataNodeMap.get(predicate);
            Optional<PredicateLevelProposal> predicateProposal = proposePerPredicate(initialNodes, predicate, dbMetadata,
                    priorityVariables);
            predicateProposal.ifPresent(proposalListBuilder::add);
        }

        return createConcreteProposal(proposalListBuilder.build(), priorityVariables);
    }

    protected abstract Optional<PredicateLevelProposal> proposePerPredicate(ImmutableCollection<DataNode> initialNodes,
                                                                  AtomPredicate predicate, DBMetadata dbMetadata,
                                                                  ImmutableList<Variable> priorityVariables) throws AtomUnificationException;

    /**
     * Assumes that the data atoms are leafs.
     *
     *
     *
     */
    private NodeCentricOptimizationResults<InnerJoinNode> applyOptimization(IntermediateQuery query,
                                                                                     QueryTreeComponent treeComponent,
                                                                                     InnerJoinNode topJoinNode,
                                                                                     ConcreteProposal proposal)
            throws EmptyQueryException {
        /**
         * First, add and remove non-top nodes
         */
        proposal.getDataNodesToRemove()
                .forEach(treeComponent::removeSubTree);

        proposal.getNewDataNodes()
                .forEach(newNode -> treeComponent.addChild(topJoinNode, newNode,
                        Optional.empty(), false));

        return updateJoinNodeAndPropagateSubstitution(query, treeComponent, topJoinNode, proposal);
    }
    
    private NodeCentricOptimizationResults<InnerJoinNode> removeSubTree(IntermediateQuery query,
                                                                        QueryTreeComponent treeComponent,
                                                                        InnerJoinNode topJoinNode) throws EmptyQueryException {
        /*
         * Replaces by an EmptyNode
         */
        EmptyNode emptyNode = iqFactory.createEmptyNode(query.getVariables(topJoinNode));
        treeComponent.replaceSubTree(topJoinNode, emptyNode);

        /*
         * Removes the empty node
         * (may throw an EmptyQuery)
         */
        RemoveEmptyNodeProposal removalProposal = new RemoveEmptyNodeProposalImpl(emptyNode, false);
        NodeTrackingResults<EmptyNode> removalResults = query.applyProposal(removalProposal);

        /*
         * If the query is not empty, changes the type of the results
         */
        return new NodeCentricOptimizationResultsImpl<>(query,
                removalResults.getOptionalNextSibling(),
                removalResults.getOptionalClosestAncestor());
    }

}
