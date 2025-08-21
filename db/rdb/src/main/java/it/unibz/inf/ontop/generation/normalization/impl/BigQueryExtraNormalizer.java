package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.transform.impl.CompositeIQTreeVariableGeneratorTransformer;

@Singleton
public class BigQueryExtraNormalizer extends CompositeIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

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
