package org.semanticweb.ontop.executor.deletion;

import org.semanticweb.ontop.pivotalrepr.*;

/**
 * TODO: explain
 */
public class ReactToChildDeletionTransformer implements HomogeneousQueryNodeTransformer {

    private final IntermediateQuery query;

    public ReactToChildDeletionTransformer(IntermediateQuery query) {
        this.query = query;
    }

    @Override
    public FilterNode transform(FilterNode filterNode) throws QueryNodeTransformationException {
        return null;
    }

    @Override
    public TableNode transform(TableNode tableNode) throws QueryNodeTransformationException {
        return null;
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) throws QueryNodeTransformationException {
        return null;
    }

    @Override
    public UnionNode transform(UnionNode unionNode) throws QueryNodeTransformationException {
        return null;
    }

    @Override
    public OrdinaryDataNode transform(OrdinaryDataNode ordinaryDataNode) throws QueryNodeTransformationException {
        return null;
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode) throws QueryNodeTransformationException {
        return null;
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode) throws QueryNodeTransformationException {
        return null;
    }

    @Override
    public GroupNode transform(GroupNode groupNode) throws QueryNodeTransformationException, NotNeededNodeException {
        return null;
    }
}
