package org.semanticweb.ontop.pivotalrepr;

public interface LeftJoinNode extends JoinLikeNode, NonCommutativeOperatorNode {

    @Override
    LeftJoinNode clone();

    @Override
    LeftJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;
}
