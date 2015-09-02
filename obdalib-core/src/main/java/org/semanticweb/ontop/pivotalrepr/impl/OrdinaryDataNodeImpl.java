package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.pivotalrepr.*;

public class OrdinaryDataNodeImpl extends DataNodeImpl implements OrdinaryDataNode {

    private static final String ORDINARY_DATA_NODE_STR = "DATA";

    public OrdinaryDataNodeImpl(DataAtom atom) {
        super(atom);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public OrdinaryDataNode clone() {
        return new OrdinaryDataNodeImpl(getAtom());
    }

    @Override
    public OrdinaryDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public QueryNode acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return ORDINARY_DATA_NODE_STR + " " + getAtom();
    }
}
