package it.unibz.inf.ontop.executor.union;

import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.impl.UnionNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.UnionLiftProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.*;

import static it.unibz.inf.ontop.pivotalrepr.unfolding.ProjectedVariableExtractionTools.extractProjectedVariables;

/**
 * TODO: explain
 */
public class UnionLiftInternalExecutor implements NodeCentricInternalExecutor<UnionNode, UnionLiftProposal> {

    @Override
    public NodeCentricOptimizationResults<UnionNode> apply(UnionLiftProposal proposal, IntermediateQuery query,
                                                           QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {

        UnionNode newUnionNode = liftUnionNode(proposal, query, treeComponent);
        return new NodeCentricOptimizationResultsImpl<>(query, newUnionNode);

    }

    private UnionNode liftUnionNode(UnionLiftProposal proposal, IntermediateQuery query,
                                           QueryTreeComponent treeComponent) {

        QueryNode targetNode = proposal.getTargetNode();
        UnionNode focusNode = proposal.getFocusNode();

        UnionNode newTopUnionNode = new UnionNodeImpl(extractProjectedVariables(query, targetNode));

        IntermediateQuery querySnapshot = query.createSnapshot();

        treeComponent.replaceSubTree(targetNode, newTopUnionNode);

        querySnapshot.getChildren(focusNode)
                .forEach(c -> appendUnionChildBranch(c, focusNode, targetNode, newTopUnionNode, query, querySnapshot,
                        treeComponent));

        return newTopUnionNode;
    }

    private void appendUnionChildBranch(QueryNode child, UnionNode focusNode,
                                        QueryNode targetNode, UnionNode newTopUnionNode, IntermediateQuery query,
                                        IntermediateQuery querySnapshot, QueryTreeComponent treeComponent) {
        Map<QueryNode, QueryNode> snapshotToQuery = new HashMap<>();

        // key : snapshot parent
        // value : query parent
        snapshotToQuery.put(focusNode, newTopUnionNode);

        // target node clone
        QueryNode targetNodeClone = targetNode.clone();
        snapshotToQuery.put(targetNode, targetNodeClone);

        treeComponent.addChild(newTopUnionNode, targetNodeClone, Optional.empty(), false);

        Queue<QueryNode> originalNodesToVisit = new LinkedList<>();
        originalNodesToVisit.addAll(querySnapshot.getChildren(targetNode));



        while (!originalNodesToVisit.isEmpty()) {
            QueryNode originalNode = originalNodesToVisit.poll();

            QueryNode originalParent =  querySnapshot.getParent(originalNode).get();
            QueryNode newParentNode = snapshotToQuery.get(originalParent);

            /**
             * TODO: explain
             *
             * Replace the focus node (union) by its child
             *
             */
            QueryNode newNode;
            if (originalNode == focusNode) {
                newNode = child;
                originalNodesToVisit.addAll(querySnapshot.getChildren(child));
                snapshotToQuery.put(child, child);
            }
            else {
                newNode = originalNode.clone();
                originalNodesToVisit.addAll(querySnapshot.getChildren(originalNode));
                snapshotToQuery.put(originalNode, newNode);
            }

            treeComponent.addChild(newParentNode, newNode,
                    querySnapshot.getOptionalPosition(originalParent, originalNode), false);

        }
    }

}
