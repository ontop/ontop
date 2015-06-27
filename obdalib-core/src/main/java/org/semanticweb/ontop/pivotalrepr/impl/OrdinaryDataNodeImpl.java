package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.pivotalrepr.*;

public class OrdinaryDataNodeImpl extends DataNodeImpl implements OrdinaryDataNode {

    public OrdinaryDataNodeImpl(DataAtom atom) {
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
    public OrdinaryDataNode clone() {
        return new OrdinaryDataNodeImpl(getAtom());
    }

    @Override
    public OrdinaryDataNode acceptNodeTransformer(QueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }
}
