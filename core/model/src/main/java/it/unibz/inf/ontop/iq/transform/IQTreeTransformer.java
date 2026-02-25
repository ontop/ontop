package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.visit.IQTreeVisitor;

@FunctionalInterface
public interface IQTreeTransformer {
    IQTree transform(IQTree tree);

    static IQTreeTransformer of(IQTreeVisitor<IQTree> visitor) {
        return t -> t.acceptVisitor(visitor);
    }

    static IQTreeTransformer of(IQTreeTransformer... transformers) {
        return t -> {
            IQTree tree = t;
            for (IQTreeTransformer transformer : transformers)
                tree = transformer.transform(tree);
            return tree;
        };
    }
}
