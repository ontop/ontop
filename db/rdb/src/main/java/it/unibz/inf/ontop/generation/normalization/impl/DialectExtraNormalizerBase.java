package it.unibz.inf.ontop.generation.normalization.impl;

import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class DialectExtraNormalizerBase implements DialectExtraNormalizer {
    private final IQTreeTransformer transformer;

    protected DialectExtraNormalizerBase(IQTreeTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public final IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transformer.transform(tree);
    }
}
