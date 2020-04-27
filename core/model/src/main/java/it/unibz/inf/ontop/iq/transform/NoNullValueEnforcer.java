package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQTree;

public interface NoNullValueEnforcer extends IQTreeTransformer {

    IQTree transform(IQTree tree);
}
