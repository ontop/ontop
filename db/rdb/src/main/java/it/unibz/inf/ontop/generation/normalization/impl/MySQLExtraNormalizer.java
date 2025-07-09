package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MySQLExtraNormalizer extends CompositeDialectNormalizer {

    @Inject
    private MySQLExtraNormalizer(OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer orderByNormalizer,
                                 ReplaceProvenanceConstantByNonGroundTermNormalizer provenanceNormalizer,
                                 ConvertValuesToUnionNormalizer toUnionNormalizer) {
        super(ImmutableList.of(
                orderByNormalizer,
                provenanceNormalizer,
                toUnionNormalizer));
    }
}
