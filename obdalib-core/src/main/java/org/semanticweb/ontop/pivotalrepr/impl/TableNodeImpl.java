package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.FunctionFreeDataAtom;
import org.semanticweb.ontop.pivotalrepr.LocalOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.QueryOptimizer;
import org.semanticweb.ontop.pivotalrepr.TableNode;

public class TableNodeImpl extends DataNodeImpl implements TableNode {
    public TableNodeImpl(FunctionFreeDataAtom atom) {
        super(atom);
    }

    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }

    @Override
    public TableNode clone() {
        return new TableNodeImpl(getAtom());
    }
}
