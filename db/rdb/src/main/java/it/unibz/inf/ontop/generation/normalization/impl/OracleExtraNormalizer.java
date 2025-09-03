package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.transform.impl.CompositeIQTreeVariableGeneratorTransformer;

@Singleton
public class OracleExtraNormalizer extends CompositeIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected OracleExtraNormalizer(OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer orderByNormalizer,
                                    WrapProjectedOrOrderByExpressionNormalizer expressionWrapper,
                                    ConvertValuesToUnionNormalizer toUnionNormalizer,
                                    UnquoteFlattenResultsNormalizer unquoteFlattenResultsNormalizer,
                                    AvoidEqualsBoolNormalizer avoidEqualsBoolNormalizer) {
        super(expressionWrapper,
                orderByNormalizer,
                toUnionNormalizer,
                unquoteFlattenResultsNormalizer,
                avoidEqualsBoolNormalizer);
    }
}
