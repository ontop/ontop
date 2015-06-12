package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.*;

/**
 *
 */
public abstract class DataNodeImpl extends QueryNodeImpl implements DataNode {

    private FunctionFreeDataAtom atom;

    protected DataNodeImpl(FunctionFreeDataAtom atom) {
        this.atom = atom;
    }

    @Override
    public FunctionFreeDataAtom getAtom() {
        return atom;
    }
}
