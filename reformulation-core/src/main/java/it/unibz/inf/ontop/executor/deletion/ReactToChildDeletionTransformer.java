package it.unibz.inf.ontop.executor.deletion;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.NodeTransformationProposalImpl;
import it.unibz.inf.ontop.pivotalrepr.*;

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
    public NodeTransformationProposal transform(ExtensionalDataNode extensionalDataNode){
        throw new UnsupportedOperationException("A TableNode is not expected to have a child");
    }

    @Override
    public NodeTransformationProposal transform(LeftJoinNode leftJoinNode){
        ImmutableList<QueryNode> children = query.getChildren(leftJoinNode);
        switch (children.size()) {
            case 0:
                // Should normally not happen
                return new NodeTransformationProposalImpl(NodeTransformationProposedState.DELETE);
            case 1:
                QueryNode remainingChild = children.get(0);
                switch(query.getOptionalPosition(leftJoinNode, remainingChild).get()) {
                    case LEFT:
                        return new NodeTransformationProposalImpl(NodeTransformationProposedState.REPLACE_BY_UNIQUE_CHILD, remainingChild);
                    /**
                     * No left-part means that the LJ will return no result.
                     */
                    case RIGHT:
                        return new NodeTransformationProposalImpl(NodeTransformationProposedState.DELETE);
                }
            default:
                return new NodeTransformationProposalImpl(NodeTransformationProposedState.NO_CHANGE);
        }
    }

    @Override
    public NodeTransformationProposal transform(UnionNode unionNode) {
        ImmutableList<QueryNode> children = query.getChildren(unionNode);
        switch (children.size()) {
            case 0:
                return new NodeTransformationProposalImpl(NodeTransformationProposedState.DELETE);
            case 1:
                return new NodeTransformationProposalImpl(NodeTransformationProposedState.REPLACE_BY_UNIQUE_CHILD, children.get(0));
            default:
                return new NodeTransformationProposalImpl(NodeTransformationProposedState.NO_CHANGE);
        }
    }

    @Override
    public NodeTransformationProposal transform(IntensionalDataNode intensionalDataNode) {
        throw new UnsupportedOperationException("A OrdinaryDataNode is not expected to have a child");
    }

    @Override
    public NodeTransformationProposal transform(InnerJoinNode innerJoinNode) {
        ImmutableList<QueryNode> children = query.getChildren(innerJoinNode);
        switch (children.size()) {
            case 0:
                return new NodeTransformationProposalImpl(NodeTransformationProposedState.DELETE);
            case 1:
                Optional<ImmutableExpression> optionalFilterCondition = innerJoinNode.getOptionalFilterCondition();
                if (optionalFilterCondition.isPresent()) {
                    return new NodeTransformationProposalImpl(NodeTransformationProposedState.REPLACE_BY_NEW_NODE,
                            new FilterNodeImpl(optionalFilterCondition.get()));
                }
                else {
                    return new NodeTransformationProposalImpl(NodeTransformationProposedState.REPLACE_BY_UNIQUE_CHILD, children.get(0));
                }
            default:
                return new NodeTransformationProposalImpl(NodeTransformationProposedState.NO_CHANGE);
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

    @Override
    public NodeTransformationProposal transform(UnsatisfiedNode unsatisfiedNode) {
        return new NodeTransformationProposalImpl(NodeTransformationProposedState.DELETE);
    }

    /**
     * TODO: find a better name
     */
    private NodeTransformationProposal checkHasChildren(QueryNode node) {
        if (hasChildren(node)) {
            return new NodeTransformationProposalImpl(NodeTransformationProposedState.NO_CHANGE);
        }
        else {
            return new NodeTransformationProposalImpl(NodeTransformationProposedState.DELETE);
        }
    }

    private boolean hasChildren(QueryNode node) {
        return !query.getChildren(node).isEmpty();
    }
}
