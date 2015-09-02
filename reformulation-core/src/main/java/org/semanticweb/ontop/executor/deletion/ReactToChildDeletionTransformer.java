package org.semanticweb.ontop.executor.deletion;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.FilterNodeImpl;

/**
 * TODO: explain
 */
public class ReactToChildDeletionTransformer
        implements HeterogeneousQueryNodeTransformer<ReactToChildDeletionTransformer.NodeToDeleteException> {

    /**
     * TODO: explain
     */
    public static class NodeToDeleteException extends QueryNodeTransformationException {
    }

    private final IntermediateQuery query;

    public ReactToChildDeletionTransformer(IntermediateQuery query) {
        this.query = query;
    }

    @Override
    public QueryNode transform(FilterNode filterNode) throws NodeToDeleteException {
        return checkHasChildren(filterNode);
    }

    @Override
    public QueryNode transform(TableNode tableNode) throws NodeToDeleteException {
        throw new UnsupportedOperationException("A TableNode is not expected to have a child");
    }

    @Override
    public QueryNode transform(LeftJoinNode leftJoinNode) throws NodeToDeleteException {
        ImmutableList<QueryNode> children = query.getCurrentSubNodesOf(leftJoinNode);
        switch (children.size()) {
            case 0:
                // Should normally not happen
                throw new NodeToDeleteException();
            case 1:
                QueryNode remainingChild = children.get(0);
                switch(query.getOptionalPosition(leftJoinNode, remainingChild).get()) {
                    case LEFT:
                        return remainingChild;
                    /**
                     * No left-part means that the LJ will return no result.
                     */
                    case RIGHT:
                        throw new NodeToDeleteException();
                }
            default:
                return leftJoinNode;
        }
    }

    @Override
    public QueryNode transform(UnionNode unionNode) throws NodeToDeleteException {
        ImmutableList<QueryNode> children = query.getCurrentSubNodesOf(unionNode);
        switch (children.size()) {
            case 0:
                throw new NodeToDeleteException();
            case 1:
                return children.get(0);
            default:
                return unionNode;
        }
    }

    @Override
    public QueryNode transform(OrdinaryDataNode ordinaryDataNode) throws NodeToDeleteException {
        throw new UnsupportedOperationException("A OrdinaryDataNode is not expected to have a child");
    }

    @Override
    public QueryNode transform(InnerJoinNode innerJoinNode) throws NodeToDeleteException {
        ImmutableList<QueryNode> children = query.getCurrentSubNodesOf(innerJoinNode);
        switch (children.size()) {
            case 0:
                throw new NodeToDeleteException();
            case 1:
                Optional<ImmutableBooleanExpression> optionalFilterCondition = innerJoinNode.getOptionalFilterCondition();
                if (optionalFilterCondition.isPresent()) {
                    return new FilterNodeImpl(optionalFilterCondition.get());
                }
                else {
                    return children.get(0);
                }
            default:
                return innerJoinNode;
        }
    }

    @Override
    public QueryNode transform(ConstructionNode constructionNode) throws NodeToDeleteException {
        return checkHasChildren(constructionNode);
    }

    @Override
    public QueryNode transform(GroupNode groupNode) throws NodeToDeleteException {
        return checkHasChildren(groupNode);
    }


    /**
     * TODO: find a better name
     */
    private QueryNode checkHasChildren(QueryNode node) throws NodeToDeleteException {
        if (hasChildren(node)) {
            return node;
        }
        else {
            throw new NodeToDeleteException();
        }
    }

    private boolean hasChildren(QueryNode node) {
        return !query.getCurrentSubNodesOf(node).isEmpty();
    }
}
