package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OracleExtraNormalizer extends CompositeDialectNormalizer {

    @Inject
    protected OracleExtraNormalizer(OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer orderByNormalizer,
                                    WrapProjectedOrOrderByExpressionNormalizer expressionWrapper,
                                    ConvertValuesToUnionNormalizer toUnionNormalizer,
                                    UnquoteFlattenResultsNormalizer unquoteFlattenResultsNormalizer,
                                    AvoidEqualsBoolNormalizer avoidEqualsBoolNormalizer) {
        super(ImmutableList.of(
                expressionWrapper,
                orderByNormalizer,
                toUnionNormalizer,
                unquoteFlattenResultsNormalizer,
                avoidEqualsBoolNormalizer));
    }
}
