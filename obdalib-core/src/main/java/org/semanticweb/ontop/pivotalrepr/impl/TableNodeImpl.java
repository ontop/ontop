package org.semanticweb.ontop.pivotalrepr.impl;


import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.pivotalrepr.*;

public class TableNodeImpl extends DataNodeImpl implements TableNode {
    private static final String TABLE_NODE_STR = "TABLE";

    public TableNodeImpl(DataAtom atom) {
        super(atom);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TableNode clone() {
        return new TableNodeImpl(getAtom());
    }

    @Override
    public TableNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public QueryNode acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return TABLE_NODE_STR + " " + getAtom();
    }
}
