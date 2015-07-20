package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: explain
 */
public interface QueryNodeTransformer {

    FilterNode transform(FilterNode filterNode) throws QueryNodeTransformationException;

    TableNode transform(TableNode tableNode) throws QueryNodeTransformationException;

    LeftJoinNode transform(LeftJoinNode leftJoinNode) throws QueryNodeTransformationException;

    UnionNode transform(UnionNode unionNode) throws QueryNodeTransformationException;

    OrdinaryDataNode transform(OrdinaryDataNode ordinaryDataNode) throws QueryNodeTransformationException;

    InnerJoinNode transform(InnerJoinNode innerJoinNode) throws QueryNodeTransformationException;

    ConstructionNode transform(ConstructionNode constructionNode) throws QueryNodeTransformationException;

    GroupNode transform(GroupNode groupNode) throws QueryNodeTransformationException, NotNeededNodeException;
}
