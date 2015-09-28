package org.semanticweb.ontop.pivotalrepr;

public class QueryNodeCloner implements HomogeneousQueryNodeTransformer {
    @Override
    public FilterNode transform(FilterNode filterNode) {
        return filterNode.clone();
    }

    @Override
    public ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) {
        return extensionalDataNode.clone();
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
    public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
        return intensionalDataNode.clone();
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
