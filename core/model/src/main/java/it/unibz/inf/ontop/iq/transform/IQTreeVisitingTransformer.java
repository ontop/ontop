package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.IQVisitor;


public interface IQTreeVisitingTransformer extends IQVisitor<IQTree>, IQTreeTransformer {

    default IQTree transformNative(NativeNode nativeNode) {
        throw new UnsupportedOperationException("NativeNode does not support transformer (too late)");
    }

    default IQTree transform(IQTree tree) {
        return tree.acceptVisitor(this);
    }
}
