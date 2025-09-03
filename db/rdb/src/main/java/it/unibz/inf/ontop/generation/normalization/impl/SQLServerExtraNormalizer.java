package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.transform.impl.CompositeIQTreeVariableGeneratorTransformer;

@Singleton
public class SQLServerExtraNormalizer extends CompositeIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected SQLServerExtraNormalizer(AlwaysProjectOrderByTermsNormalizer projectOrderByTermsNormalizer,
                                       WrapProjectedOrOrderByExpressionNormalizer projectionWrapper,
                                       SQLServerLimitOffsetOldVersionNormalizer limitOffsetOldVersionNormalizer,
                                       SQLServerInsertOrderByInSliceNormalizer insertOrderByInSlizeNormalizer,
                                       AvoidEqualsBoolNormalizer avoidEqualsBoolNormalizer) {
        super(projectionWrapper,
                projectOrderByTermsNormalizer,
                limitOffsetOldVersionNormalizer,
                insertOrderByInSlizeNormalizer,
                avoidEqualsBoolNormalizer);
    }
}
