package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;

@Singleton
public class BigQueryExtraNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected BigQueryExtraNormalizer(AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer,
                                      OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer pushProjectedOrderByTermsNormalizer,
                                      ConvertValuesToUnionNormalizer convertValuesToUnionNormalizer) {
        super(convertValuesToUnionNormalizer,
                alwaysProjectOrderByTermsNormalizer,
                pushProjectedOrderByTermsNormalizer);
    }
}
