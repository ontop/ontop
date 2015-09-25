package org.semanticweb.ontop.pivotalrepr.impl;


import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.pivotalrepr.*;

public class TableNodeImpl extends ExtensionalDataNodeImpl implements TableNode {
    private static final String TABLE_NODE_STR = "TABLE";

    public TableNodeImpl(DataAtom atom) {
        super(atom);
    }


    @Override
    public TableNode clone() {
        return new TableNodeImpl(getAtom());
    }

    @Override
    public String toString() {
        return TABLE_NODE_STR + " " + getAtom();
    }
}
