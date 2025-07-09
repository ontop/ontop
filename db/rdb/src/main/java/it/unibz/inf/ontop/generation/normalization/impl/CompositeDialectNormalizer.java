package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class CompositeDialectNormalizer implements DialectExtraNormalizer {
    private final ImmutableList<DialectExtraNormalizer> normalizers;

    protected CompositeDialectNormalizer(ImmutableList<DialectExtraNormalizer> normalizers) {
        this.normalizers = normalizers;
    }

    @Override
    public IQTree transform(IQTree initial, VariableGenerator variableGenerator) {
        // non-final
        IQTree tree = initial;
        for (DialectExtraNormalizer normalizer : normalizers) {
            tree = normalizer.transform(tree, variableGenerator);
        }
        return tree;
    }
}
