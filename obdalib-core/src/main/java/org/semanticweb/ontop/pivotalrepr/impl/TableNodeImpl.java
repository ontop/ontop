package org.semanticweb.ontop.pivotalrepr.impl;


import org.semanticweb.ontop.pivotalrepr.FunctionFreeDataAtom;
import org.semanticweb.ontop.pivotalrepr.TableNode;

public class TableNodeImpl extends DataNodeImpl implements TableNode {
    public TableNodeImpl(FunctionFreeDataAtom atom) {
        super(atom);
    }

    @Override
    public TableNode clone() {
        return new TableNodeImpl(getAtom());
    }
}
