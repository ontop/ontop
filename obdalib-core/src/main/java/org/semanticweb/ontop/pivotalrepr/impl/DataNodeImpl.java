package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.*;

/**
 *
 */
public abstract class DataNodeImpl extends QueryNodeImpl implements DataNode {

    private DataAtom atom;

    protected DataNodeImpl(DataAtom atom) {
        this.atom = atom;
    }

    @Override
    public DataAtom getAtom() {
        return atom;
    }
}
