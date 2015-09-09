package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: explain
 */
public interface TableNode extends DataNode {

    @Override
    TableNode clone();

    @Override
    TableNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;
}
