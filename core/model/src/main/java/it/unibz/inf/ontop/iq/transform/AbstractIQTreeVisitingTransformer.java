package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.impl.AbstractIQVisitor;


public abstract class AbstractIQTreeVisitingTransformer extends AbstractIQVisitor<IQTree> {

    @Override
    public IQTree transformNative(NativeNode nativeNode) {
        throw new UnsupportedOperationException("NativeNode does not support transformer (too late)");
    }
}
