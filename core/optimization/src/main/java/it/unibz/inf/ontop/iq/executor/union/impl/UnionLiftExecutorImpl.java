package it.unibz.inf.ontop.iq.executor.union.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.executor.union.UnionLiftExecutor;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.UnionLiftProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.*;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

/**
 * TODO: explain
 */
public class UnionLiftExecutorImpl implements UnionLiftExecutor {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private UnionLiftExecutorImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public NodeCentricOptimizationResults<UnionNode> apply(UnionLiftProposal proposal, IntermediateQuery query,
                                                           QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {

        validateProposal(proposal, query);

        UnionNode newUnionNode = liftUnionNode(proposal, query, treeComponent);
        return new NodeCentricOptimizationResultsImpl<>(query, newUnionNode);

    }

    private void validateProposal(UnionLiftProposal proposal, IntermediateQuery query)
            throws InvalidQueryOptimizationProposalException {
        UnionNode focusNode = proposal.getFocusNode();
        QueryNode targetNode = proposal.getTargetNode();

        if (targetNode instanceof UnionNode) {
            throw new InvalidQueryOptimizationProposalException("The target node of UnionLiftProposal cannot be an union node");
        }

        if (targetNode instanceof LeftJoinNode &&
                getDescendantPosition((LeftJoinNode)targetNode, focusNode, query) == RIGHT
                ) {
            throw new InvalidQueryOptimizationProposalException("Lifting a UNION from the right part of a LJ is not allowed");
        }
        else if (!query.hasAncestor(focusNode, targetNode)) {
            throw new InvalidQueryOptimizationProposalException("The focus must be a descendant of the target node");
        }
    }

    private ArgumentPosition getDescendantPosition(LeftJoinNode ancestorNode, QueryNode descendantNode, IntermediateQuery query)
        throws InvalidQueryOptimizationProposalException{

        // Non-final
        Optional<QueryNode> optionalCurrentAncestor = query.getParent(descendantNode);
        QueryNode currentChild = descendantNode;

        while (optionalCurrentAncestor.isPresent()) {
            QueryNode currentAncestor = optionalCurrentAncestor.get();
            if (currentAncestor == ancestorNode) {
                return query.getOptionalPosition(currentAncestor, currentChild)
                        .orElseThrow(() -> new IllegalStateException("The child of a LJ must have a position") );
            }
            optionalCurrentAncestor = query.getParent(currentAncestor);
            currentChild = currentAncestor;
        }
        throw new InvalidQueryOptimizationProposalException("The focus must be a descendant of the target node");
    }

    private UnionNode liftUnionNode(UnionLiftProposal proposal, IntermediateQuery query,
                                           QueryTreeComponent treeComponent) {

        QueryNode targetNode = proposal.getTargetNode();
        UnionNode focusNode = proposal.getFocusNode();

        UnionNode newTopUnionNode = iqFactory.createUnionNode(query.getVariables(targetNode));

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
