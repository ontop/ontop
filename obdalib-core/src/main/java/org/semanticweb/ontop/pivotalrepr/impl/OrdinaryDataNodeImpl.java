package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.DataAtom;
import org.semanticweb.ontop.pivotalrepr.OrdinaryDataNode;

public class OrdinaryDataNodeImpl extends DataNodeImpl implements OrdinaryDataNode {

    public OrdinaryDataNodeImpl(DataAtom atom) {
        super(atom);
    }

    @Override
    public OrdinaryDataNode clone() {
        return new OrdinaryDataNodeImpl(getAtom());
    }
}
