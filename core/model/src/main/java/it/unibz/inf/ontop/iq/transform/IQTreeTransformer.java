package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

@FunctionalInterface
public interface IQTreeTransformer {
    IQTree transform(IQTree tree);

    static IQTreeTransformer of(IQVisitor<IQTree> visitor) {
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
