package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.*;

public class TableNodeImpl extends DataNodeImpl implements TableNode {
    public TableNodeImpl(DataAtom atom) {
        super(atom);
    }

    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TableNode clone() {
        return new TableNodeImpl(getAtom());
    }
}
