package org.semanticweb.ontop.pivotalrepr;

/**
 * GROUP BY query node.
 */
public interface GroupNode extends QueryNode {

    // TODO: extend


    @Override
    GroupNode clone();

    @Override
    GroupNode acceptNodeTransformer(QueryNodeTransformer transformer) throws QueryNodeTransformationException;
}
