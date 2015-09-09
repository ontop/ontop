package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.*;

public class NonStandardNodeImpl extends QueryNodeImpl implements NonStandardNode {
    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NonStandardNode clone() {
        throw new UnsupportedOperationException("This method must be override by a sub-class.");
    }

    @Override
    public NonStandardNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException, NotNeededNodeException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }
}
