package org.semanticweb.ontop.executor.deletion;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.FilterNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.NodeTransformationProposalImpl;

import static org.semanticweb.ontop.pivotalrepr.NodeTransformationProposedState.*;

/**
 * TODO: explain
 */
public class ReactToChildDeletionTransformer implements HeterogeneousQueryNodeTransformer {

    private final IntermediateQuery query;

    public ReactToChildDeletionTransformer(IntermediateQuery query) {
        this.query = query;
    }

    @Override
    public NodeTransformationProposal transform(FilterNode filterNode) {
        return checkHasChildren(filterNode);
    }

    @Override
    public NodeTransformationProposal transform(TableNode tableNode){
        throw new UnsupportedOperationException("A TableNode is not expected to have a child");
    }

    @Override
    public NodeTransformationProposal transform(LeftJoinNode leftJoinNode){
        ImmutableList<QueryNode> children = query.getCurrentSubNodesOf(leftJoinNode);
        switch (children.size()) {
            case 0:
                // Should normally not happen
                return new NodeTransformationProposalImpl(DELETE);
            case 1:
                QueryNode remainingChild = children.get(0);
                switch(query.getOptionalPosition(leftJoinNode, remainingChild).get()) {
                    case LEFT:
                        return new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_CHILD, remainingChild);
                    /**
                     * No left-part means that the LJ will return no result.
                     */
                    case RIGHT:
                        return new NodeTransformationProposalImpl(DELETE);
                }
            default:
                return new NodeTransformationProposalImpl(NO_CHANGE);
        }
    }

    @Override
    public NodeTransformationProposal transform(UnionNode unionNode) {
        ImmutableList<QueryNode> children = query.getCurrentSubNodesOf(unionNode);
        switch (children.size()) {
            case 0:
                return new NodeTransformationProposalImpl(DELETE);
            case 1:
                return new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_CHILD, children.get(0));
            default:
                return new NodeTransformationProposalImpl(NO_CHANGE);
        }
    }

    @Override
    public NodeTransformationProposal transform(OrdinaryDataNode ordinaryDataNode) {
        throw new UnsupportedOperationException("A OrdinaryDataNode is not expected to have a child");
    }

    @Override
    public NodeTransformationProposal transform(InnerJoinNode innerJoinNode) {
        ImmutableList<QueryNode> children = query.getCurrentSubNodesOf(innerJoinNode);
        switch (children.size()) {
            case 0:
                return new NodeTransformationProposalImpl(DELETE);
            case 1:
                Optional<ImmutableBooleanExpression> optionalFilterCondition = innerJoinNode.getOptionalFilterCondition();
                if (optionalFilterCondition.isPresent()) {
                    return new NodeTransformationProposalImpl(REPLACE_BY_NEW_NODE,
                            new FilterNodeImpl(optionalFilterCondition.get()));
                }
                else {
                    return new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_CHILD, children.get(0));
                }
            default:
                return new NodeTransformationProposalImpl(NO_CHANGE);
        }
    }

    @Override
    public NodeTransformationProposal transform(ConstructionNode constructionNode) {
        return checkHasChildren(constructionNode);
    }

    @Override
    public NodeTransformationProposal transform(GroupNode groupNode) {
        return checkHasChildren(groupNode);
    }

    /**
     * TODO: find a better name
     */
    private NodeTransformationProposal checkHasChildren(QueryNode node) {
        if (hasChildren(node)) {
            return new NodeTransformationProposalImpl(NO_CHANGE);
        }
        else {
            return new NodeTransformationProposalImpl(DELETE);
        }
    }

    private boolean hasChildren(QueryNode node) {
        return !query.getCurrentSubNodesOf(node).isEmpty();
    }
}
