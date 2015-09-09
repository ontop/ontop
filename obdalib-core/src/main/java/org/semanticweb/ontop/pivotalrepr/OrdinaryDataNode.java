package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: explain
 * TODO: find a better name
 */
public interface OrdinaryDataNode extends DataNode {

    @Override
    OrdinaryDataNode clone();

    @Override
    OrdinaryDataNode acceptNodeTransformer(QueryNodeTransformer transformer) throws QueryNodeTransformationException;
}
