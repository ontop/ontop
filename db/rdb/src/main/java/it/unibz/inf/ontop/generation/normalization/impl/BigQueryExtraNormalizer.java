package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BigQueryExtraNormalizer extends CompositeDialectNormalizer {

    @Inject
    protected BigQueryExtraNormalizer(AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer,
                                      OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer pushProjectedOrderByTermsNormalizer,
                                      ConvertValuesToUnionNormalizer convertValuesToUnionNormalizer) {
        super(ImmutableList.of(
                convertValuesToUnionNormalizer,
                alwaysProjectOrderByTermsNormalizer,
                pushProjectedOrderByTermsNormalizer));
    }
}
