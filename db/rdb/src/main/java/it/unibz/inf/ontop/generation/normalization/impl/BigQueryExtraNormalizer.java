package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class BigQueryExtraNormalizer implements DialectExtraNormalizer {

    private final AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer;
    private final OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer pushProjectedOrderByTermsNormalizer;
    private final ConvertValuesToUnionNormalizer convertValuesToUnionNormalizer;

    @Inject
    protected BigQueryExtraNormalizer(AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer,
                                      OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer pushProjectedOrderByTermsNormalizer,
                                      ConvertValuesToUnionNormalizer convertValuesToUnionNormalizer) {
        this.alwaysProjectOrderByTermsNormalizer = alwaysProjectOrderByTermsNormalizer;
        this.pushProjectedOrderByTermsNormalizer = pushProjectedOrderByTermsNormalizer;
        this.convertValuesToUnionNormalizer = convertValuesToUnionNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
              return pushProjectedOrderByTermsNormalizer.transform(
                      alwaysProjectOrderByTermsNormalizer.transform(
                              convertValuesToUnionNormalizer.transform(tree, variableGenerator),
                              variableGenerator),
                      variableGenerator);
    }
}
