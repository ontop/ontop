package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;

@Singleton
public class MySQLExtraNormalizer implements DialectExtraNormalizer {

    private final DialectExtraNormalizer orderByNormalizer;
    private final ConvertValuesToUnionNormalizer toUnionNormalizer;

    @Inject
    protected MySQLExtraNormalizer(OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer orderByNormalizer,
                                    ConvertValuesToUnionNormalizer toUnionNormalizer) {
        this.orderByNormalizer = orderByNormalizer;

        // Note: MySQL does support VALUES (and thus in theory does not need this normalizer)
        // but only as of version 8.0.19
        this.toUnionNormalizer = toUnionNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return toUnionNormalizer.transform(
                orderByNormalizer.transform(
                        tree,
                        variableGenerator),
                variableGenerator);
    }
}
