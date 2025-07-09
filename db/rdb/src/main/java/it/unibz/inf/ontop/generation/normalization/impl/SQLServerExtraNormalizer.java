package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SQLServerExtraNormalizer extends CompositeDialectNormalizer {

    @Inject
    protected SQLServerExtraNormalizer(AlwaysProjectOrderByTermsNormalizer projectOrderByTermsNormalizer,
                                       WrapProjectedOrOrderByExpressionNormalizer projectionWrapper,
                                       SQLServerLimitOffsetOldVersionNormalizer limitOffsetOldVersionNormalizer,
                                       SQLServerInsertOrderByInSliceNormalizer insertOrderByInSlizeNormalizer,
                                       AvoidEqualsBoolNormalizer avoidEqualsBoolNormalizer) {
        super(ImmutableList.of(
                projectionWrapper,
                projectOrderByTermsNormalizer,
                limitOffsetOldVersionNormalizer,
                insertOrderByInSlizeNormalizer,
                avoidEqualsBoolNormalizer));
    }
}
