package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DenodoExtraNormalizer extends CompositeDialectNormalizer {

    @Inject
    protected DenodoExtraNormalizer(AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer,
                                    ConvertValuesToUnionNormalizer toUnionNormalizer,
                                    AlwaysPushProjectedOrderByTermsNormalizer pushProjectedOrderByTermsNormalizer,
                                    SplitIsNullOverConjunctionDisjunctionNormalizer splitIsNullOverConjunctionDisjunctionNormalizer,
                                    EliminateLimitsFromSubQueriesNormalizer eliminateLimitsFromSubQueriesNormalizer) {
        super(ImmutableList.of(
                alwaysProjectOrderByTermsNormalizer,
                toUnionNormalizer,
                pushProjectedOrderByTermsNormalizer,
                splitIsNullOverConjunctionDisjunctionNormalizer,
                eliminateLimitsFromSubQueriesNormalizer));
    }
}

