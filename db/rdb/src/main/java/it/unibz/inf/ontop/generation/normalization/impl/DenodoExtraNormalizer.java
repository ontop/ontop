package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.transform.impl.CompositeIQTreeVariableGeneratorTransformer;

@Singleton
public class DenodoExtraNormalizer extends CompositeIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected DenodoExtraNormalizer(AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer,
                                    ConvertValuesToUnionNormalizer toUnionNormalizer,
                                    AlwaysPushProjectedOrderByTermsNormalizer pushProjectedOrderByTermsNormalizer,
                                    SplitIsNullOverConjunctionDisjunctionNormalizer splitIsNullOverConjunctionDisjunctionNormalizer,
                                    EliminateLimitsFromSubQueriesNormalizer eliminateLimitsFromSubQueriesNormalizer) {
        super(alwaysProjectOrderByTermsNormalizer,
                toUnionNormalizer,
                pushProjectedOrderByTermsNormalizer,
                splitIsNullOverConjunctionDisjunctionNormalizer,
                eliminateLimitsFromSubQueriesNormalizer);
    }
}

