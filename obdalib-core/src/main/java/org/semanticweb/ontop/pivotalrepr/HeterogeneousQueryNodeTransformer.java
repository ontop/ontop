package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: explain
 */
public interface HeterogeneousQueryNodeTransformer<P extends NodeTransformationProposal> {

    P transform(FilterNode filterNode);

    P transform(TableNode tableNode);

    P transform(LeftJoinNode leftJoinNode);

    P transform(UnionNode unionNode);

    P transform(OrdinaryDataNode ordinaryDataNode);

    P transform(InnerJoinNode innerJoinNode);

    P transform(ConstructionNode constructionNode);

    P transform(GroupNode groupNode);

}
