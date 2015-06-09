package org.semanticweb.ontop.pivotalrepr.impl;


import org.semanticweb.ontop.pivotalrepr.DataAtom;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.TableNode;

public class TableNodeImpl extends DataNodeImpl implements TableNode {
    public TableNodeImpl(DataAtom atom) {
        super(atom);
    }

    @Override
    public TableNode clone() {
        return new TableNodeImpl(getAtom());
    }
}
