package org.semanticweb.ontop.pivotalrepr;

public interface InnerJoinNode extends JoinLikeNode {

    @Override
    InnerJoinNode clone();

    @Override
    InnerJoinNode acceptNodeTransformer(QueryNodeTransformer transformer)
            throws QueryNodeTransformationException;
}
