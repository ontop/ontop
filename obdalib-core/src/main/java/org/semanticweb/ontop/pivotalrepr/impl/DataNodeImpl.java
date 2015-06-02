package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.DataAtom;
import org.semanticweb.ontop.pivotalrepr.DataNode;
import org.semanticweb.ontop.pivotalrepr.LocalOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.QueryOptimizer;

/**
 *
 */
public abstract class DataNodeImpl extends QueryNodeImpl implements DataNode {

    private DataAtom atom;

    protected DataNodeImpl(DataAtom atom) {
        this.atom = atom;
    }

    public DataAtom getAtom() {
        return atom;
    }

    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        // TODO: implement it
        return null;
    }
}
