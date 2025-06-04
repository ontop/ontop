package it.unibz.inf.ontop.iq.transform.impl;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

public class IQTreeTransformerAdapter implements IQTreeTransformer {
    private final IQVisitor<IQTree> visitor;

    public IQTreeTransformerAdapter(IQVisitor<IQTree> visitor) {
        this.visitor = visitor;
    }

    @Override
    public IQTree transform(IQTree tree) {
        return tree.acceptVisitor(visitor);
    }
}
