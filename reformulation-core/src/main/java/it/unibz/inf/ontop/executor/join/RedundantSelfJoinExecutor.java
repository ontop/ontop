package it.unibz.inf.ontop.executor.join;

import com.google.common.collect.*;
import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableUnificationTools;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.ExtensionalDataNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.SubstitutionPropagationProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Assumption: clean inner join structure (an inner join does not have another inner join or filter node as a child).
 *
 * Naturally assumes that the data atoms are leafs.
 *
 */
public class RedundantSelfJoinExecutor extends SelfJoinLikeExecutor implements InnerJoinExecutor {

    /**
     * Safety, to prevent infinite loops
     */
    private static final int MAX_ITERATIONS = 100;


    @Override
    public NodeCentricOptimizationResults<InnerJoinNode> apply(final InnerJoinOptimizationProposal highLevelProposal,
                                                final IntermediateQuery query,
                                                final QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {

        // Non-final
        InnerJoinNode topJoinNode = highLevelProposal.getFocusNode();

        ImmutableMultimap<AtomPredicate, DataNode> initialMap = extractDataNodes(query.getChildren(topJoinNode));

        /**
         * Tries to optimize if there are data nodes
         */
        int i=0;
        while (!initialMap.isEmpty() && (i++ < MAX_ITERATIONS)) {

            // TODO: explain
            ImmutableSet<Variable> variablesToKeep = query.getClosestConstructionNode(topJoinNode).getLocalVariables();

            Optional<ConcreteProposal> optionalConcreteProposal = propose(initialMap, variablesToKeep,
                    query.getMetadata().getUniqueConstraints());

            if (optionalConcreteProposal.isPresent()) {
                ConcreteProposal concreteProposal = optionalConcreteProposal.get();

                // SIDE-EFFECT on the tree component (and thus on the query)
                NodeCentricOptimizationResults<InnerJoinNode> result = applyOptimization(query, treeComponent,
                        topJoinNode, concreteProposal);

                /**
                 *
                 */
                if (result.getOptionalNewNode().isPresent()) {
                    int oldSize = initialMap.size();
                    initialMap = extractDataNodes(result.getResultingQuery().getChildren(
                            result.getOptionalNewNode().get()));
                    int newSize = initialMap.size();

                    if (oldSize == newSize) {
                        return result;
                    }
                    else if (oldSize < newSize) {
                        throw new IllegalStateException("The number of data atoms was expected to decrease, not increase");
                    }
                    // else, continue
                    topJoinNode = result.getOptionalNewNode().get();

                } else {
                    return result;
                }
            }
        }

        /**
         * Safety
         */
        if (i >= MAX_ITERATIONS) {
            throw new IllegalStateException("Redundant self-join elimination loop has reached " +
                    "the max iteration threshold (" + MAX_ITERATIONS + ")");
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
                                                       ImmutableCollection<ImmutableList<Integer>> primaryKeyPositions)
            throws AtomUnificationException {
        final ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMap
                = groupByPrimaryKeyArguments(dataNodes, primaryKeyPositions);

        return proposeForGroupingMap(groupingMap);
    }

    /**
     * dataNodes and primaryKeyPositions are given for the same predicate
     * TODO: explain
     */
    private static ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupByPrimaryKeyArguments(
            ImmutableCollection<DataNode> dataNodes,
            ImmutableCollection<ImmutableList<Integer>> collectionOfPrimaryKeyPositions) {
        ImmutableMultimap.Builder<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMapBuilder = ImmutableMultimap.builder();

        for (ImmutableList<Integer> primaryKeyPositions : collectionOfPrimaryKeyPositions) {
            for (DataNode dataNode : dataNodes) {
                groupingMapBuilder.put(extractPrimaryKeyArguments(dataNode.getProjectionAtom(), primaryKeyPositions), dataNode);
            }
        }
        return groupingMapBuilder.build();
    }

    /**
     * Assumes that the data atoms are leafs.
     *
     *
     *
     */
    private NodeCentricOptimizationResults<InnerJoinNode> applyOptimization(IntermediateQuery query,
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

        return getJoinNodeCentricOptimizationResults(query, treeComponent, topJoinNode, proposal);
    }


}
