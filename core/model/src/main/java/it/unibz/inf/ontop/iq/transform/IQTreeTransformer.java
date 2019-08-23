package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQTree;

@FunctionalInterface
public interface IQTreeTransformer {

    IQTree transform(IQTree tree);
}
