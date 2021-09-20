package it.unibz.inf.ontop.generation.normalization.impl;

import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DenodoExtraNormalizer implements DialectExtraNormalizer {

    private final AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer;
    private final ConvertValuesToUnionNormalizer toUnionNormalizer;

    @Inject
    protected DenodoExtraNormalizer(AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer,
                                    ConvertValuesToUnionNormalizer toUnionNormalizer) {
        this.alwaysProjectOrderByTermsNormalizer = alwaysProjectOrderByTermsNormalizer;
        this.toUnionNormalizer = toUnionNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return toUnionNormalizer.transform(
                alwaysProjectOrderByTermsNormalizer.transform(tree, variableGenerator),
                variableGenerator);
    }
}

