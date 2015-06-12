package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.FunctionFreeDataAtom;
import org.semanticweb.ontop.pivotalrepr.PureDataAtom;
import org.semanticweb.ontop.pivotalrepr.OrdinaryDataNode;

public class OrdinaryDataNodeImpl extends DataNodeImpl implements OrdinaryDataNode {

    public OrdinaryDataNodeImpl(FunctionFreeDataAtom atom) {
        super(atom);
    }

    @Override
    public OrdinaryDataNode clone() {
        return new OrdinaryDataNodeImpl(getAtom());
    }
}
