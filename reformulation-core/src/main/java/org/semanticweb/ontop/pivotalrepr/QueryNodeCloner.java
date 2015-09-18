package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.pivotalrepr.impl.HomogeneousQueryNodeTransformerImpl;

public class QueryNodeCloner extends HomogeneousQueryNodeTransformerImpl {
    @Override
    public FilterNode transform(FilterNode filterNode) {
        return filterNode.clone();
    }

    @Override
    public TableNode transform(TableNode tableNode) {
        return tableNode.clone();
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return leftJoinNode.clone();
    }

    @Override
    public UnionNode transform(UnionNode unionNode) {
        return unionNode.clone();
    }

    @Override
    public OrdinaryDataNode transform(OrdinaryDataNode ordinaryDataNode) {
        return ordinaryDataNode.clone();
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode) {
        return innerJoinNode.clone();
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode) {
        return constructionNode.clone();
    }

    @Override
    public GroupNode transform(GroupNode groupNode) {
        return groupNode.clone();
    }
}
