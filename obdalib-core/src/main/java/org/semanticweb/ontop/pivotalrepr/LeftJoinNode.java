package org.semanticweb.ontop.pivotalrepr;

public interface LeftJoinNode extends JoinLikeNode, BinaryAsymmetricOperatorNode {

    @Override
    LeftJoinNode clone();

    @Override
    LeftJoinNode acceptNodeTransformer(QueryNodeTransformer transformer) throws QueryNodeTransformationException;
}
