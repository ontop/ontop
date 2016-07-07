package it.unibz.inf.ontop.executor.leftjoin;

import com.google.common.collect.*;
import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.executor.join.SelfJoinLikeExecutor;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.InnerJoinNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.sql.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 * TODO: explain
 *
 * Assumption: clean inner join structure (an inner join does not have another inner join or filter node as a child).
 *
 * Naturally assumes that the data atoms are leafs.
 *
 */
public class RedundantSelfLeftJoinExecutor
        extends SelfJoinLikeExecutor
        implements NodeCentricInternalExecutor<LeftJoinNode, LeftJoinOptimizationProposal> {

    @Override
    public NodeCentricOptimizationResults<LeftJoinNode>
    apply(LeftJoinOptimizationProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        LeftJoinNode leftJoinNode = proposal.getFocusNode();

        QueryNode leftChild = query.getChild(leftJoinNode,LEFT).orElseThrow(() -> new IllegalStateException("The left child of a LJ is missing: " + leftJoinNode ));
        QueryNode rightChild = query.getChild(leftJoinNode,RIGHT).orElseThrow(() -> new IllegalStateException("The right child of a LJ is missing: " + leftJoinNode));

        if (leftChild instanceof DataNode && rightChild instanceof DataNode) {

            DataNode leftDataNode = (DataNode) leftChild;
            DataNode rightDataNode = (DataNode) rightChild;

            // TODO: explain
            ImmutableSet<Variable> variablesToKeep = query.getClosestConstructionNode(leftJoinNode).getVariables();

            Optional<ConcreteProposal> optionalConcreteProposal = propose(leftDataNode, rightDataNode, variablesToKeep,
                    query.getMetadata());

            if (optionalConcreteProposal.isPresent()) {
                ConcreteProposal concreteProposal = optionalConcreteProposal.get();

                // SIDE-EFFECT on the tree component (and thus on the query)
                return applyOptimization(query, treeComponent, leftJoinNode, concreteProposal);
            }
        }

        // No optimization
        return new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode);
    }


    /**
     *  Assumes that LeftJoin has only two children, one left and one right.
     *
     *  Returns a proposal for optimization.
     */
    private Optional<ConcreteProposal> propose(DataNode leftDataNode, DataNode rightDataNode,
                                               ImmutableSet<Variable> variablesToKeep,
                                               MetadataForQueryOptimization metadata) {

        AtomPredicate leftPredicate = leftDataNode.getProjectionAtom().getPredicate();
        AtomPredicate rightPredicate = rightDataNode.getProjectionAtom().getPredicate();

        ImmutableList<DataNode> initialNodes = ImmutableList.of(leftDataNode, rightDataNode);

        PredicateLevelProposal predicateProposal;
        if(leftPredicate.equals(rightPredicate)) {
            /**
             * the left and the right predicates are the same,
             * so we deal with self left join
             */
            if (metadata.getUniqueConstraints().containsKey(leftPredicate)) {
                try {
                    predicateProposal = proposeForSelfLeftJoin(
                            leftDataNode,
                            rightDataNode,
                            metadata.getUniqueConstraints().get(leftPredicate));
                } catch  (AtomUnificationException e) {
                    predicateProposal = new PredicateLevelProposal(initialNodes);
                }
            }
            else {
                predicateProposal = new PredicateLevelProposal(initialNodes);
            }
        }
        else {
            // TODO: that is a weird way of dealing with no optimization. Change it
            predicateProposal = new PredicateLevelProposal(initialNodes);
        }

        return createConcreteProposal(ImmutableList.of(predicateProposal), variablesToKeep);

    }


    private PredicateLevelProposal proposeForSelfLeftJoin(
            DataNode leftDataNode,
            DataNode rightDataNode,
            ImmutableCollection<ImmutableList<Integer>> collectionOfPrimaryKeyPositions)
            throws AtomUnificationException {

        ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMap =
                groupByPrimaryKeyArguments(leftDataNode, rightDataNode, collectionOfPrimaryKeyPositions);

        return proposeForGroupingMap(groupingMap);
    }

    /**
     * left and right data nodes and collectionOfPrimaryKeyPositions are given for the same predicate
     * TODO: explain and rename
     */
    private static ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupByPrimaryKeyArguments(
            DataNode leftDataNode,
            DataNode rightDataNode,
            ImmutableCollection<ImmutableList<Integer>> collectionOfPrimaryKeyPositions) {
        ImmutableMultimap.Builder<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMapBuilder = ImmutableMultimap.builder();

        for (ImmutableList<Integer> primaryKeyPositions : collectionOfPrimaryKeyPositions) {
            groupingMapBuilder.put(extractPrimaryKeyArguments(leftDataNode.getProjectionAtom(), primaryKeyPositions), leftDataNode);
            groupingMapBuilder.put(extractPrimaryKeyArguments(rightDataNode.getProjectionAtom(), primaryKeyPositions), rightDataNode);
        }
        return groupingMapBuilder.build();
    }

    /**
     * Assumes that the data atoms are leafs.
     *
     */
    private NodeCentricOptimizationResults<LeftJoinNode> applyOptimization(IntermediateQuery query,
                                                                           QueryTreeComponent treeComponent,
                                                                           LeftJoinNode leftJoinNode,
                                                                           ConcreteProposal proposal) {
        /**
         * First, add and remove non-top nodes
         */
        proposal.getDataNodesToRemove()
                .forEach(treeComponent::removeSubTree);

        switch( proposal.getNewDataNodes().size() ) {
            case 0:
                break;

            case 1:
                proposal.getNewDataNodes()
                        .forEach(newNode -> treeComponent.addChild(leftJoinNode, newNode,
                                Optional.of(LEFT), false));
                break;

            case 2:
                UnmodifiableIterator<DataNode> dataNodeIter = proposal.getNewDataNodes().iterator();
                treeComponent.addChild(leftJoinNode, dataNodeIter.next(), Optional.of(LEFT), false);
                treeComponent.addChild(leftJoinNode, dataNodeIter.next(), Optional.of(RIGHT), false);
                break;

            default:
                throw new IllegalStateException("Self-left join elimination MUST not add more than 2 new nodes");
        }

        return getJoinNodeCentricOptimizationResults(query, treeComponent, leftJoinNode, proposal);
    }



}
