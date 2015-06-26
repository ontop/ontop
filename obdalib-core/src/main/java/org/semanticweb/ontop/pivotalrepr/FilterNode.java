package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: explain
 */
public interface FilterNode extends JoinOrFilterNode {

    @Override
    FilterNode clone();

    @Override
    FilterNode acceptNodeTransformer(QueryNodeTransformer transformer) throws QueryNodeTransformationException;
}
