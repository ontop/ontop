package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: explain
 */
public interface HomogeneousQueryNodeTransformer<T1 extends QueryNodeTransformationException,
        T2 extends QueryNodeTransformationException> {

    FilterNode transform(FilterNode filterNode) throws T1, T2;

    ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) throws T1, T2;

    LeftJoinNode transform(LeftJoinNode leftJoinNode) throws T1, T2;

    UnionNode transform(UnionNode unionNode) throws T1, T2;

    IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) throws T1, T2;

    InnerJoinNode transform(InnerJoinNode innerJoinNode) throws T1, T2;

    ConstructionNode transform(ConstructionNode constructionNode) throws T1, T2;

    GroupNode transform(GroupNode groupNode) throws T1, T2, NotNeededNodeException;
}
