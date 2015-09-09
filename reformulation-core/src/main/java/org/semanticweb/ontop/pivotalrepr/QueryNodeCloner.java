package org.semanticweb.ontop.pivotalrepr;

public class QueryNodeCloner implements QueryNodeTransformer {
    @Override
    public FilterNode transform(FilterNode filterNode) throws QueryNodeTransformationException {
        return filterNode.clone();
    }

    @Override
    public TableNode transform(TableNode tableNode) throws QueryNodeTransformationException {
        return tableNode.clone();
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) throws QueryNodeTransformationException {
        return leftJoinNode.clone();
    }

    @Override
    public UnionNode transform(UnionNode unionNode) throws QueryNodeTransformationException {
        return unionNode.clone();
    }

    @Override
    public OrdinaryDataNode transform(OrdinaryDataNode ordinaryDataNode) throws QueryNodeTransformationException {
        return ordinaryDataNode.clone();
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode) throws QueryNodeTransformationException {
        return innerJoinNode.clone();
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode) throws QueryNodeTransformationException {
        return constructionNode.clone();
    }

    @Override
    public GroupNode transform(GroupNode groupNode) throws QueryNodeTransformationException, NotNeededNodeException {
        return groupNode.clone();
    }
}
