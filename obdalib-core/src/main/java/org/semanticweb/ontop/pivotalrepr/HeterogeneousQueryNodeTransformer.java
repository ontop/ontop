package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: explain
 */
public interface HeterogeneousQueryNodeTransformer<T extends QueryNodeTransformationException> {

    QueryNode transform(FilterNode filterNode) throws T;

    QueryNode transform(TableNode tableNode) throws T;

    QueryNode transform(LeftJoinNode leftJoinNode) throws T;

    QueryNode transform(UnionNode unionNode) throws T;

    QueryNode transform(OrdinaryDataNode ordinaryDataNode) throws T;

    QueryNode transform(InnerJoinNode innerJoinNode) throws T;

    QueryNode transform(ConstructionNode constructionNode) throws T;

    QueryNode transform(GroupNode groupNode) throws T;

}
