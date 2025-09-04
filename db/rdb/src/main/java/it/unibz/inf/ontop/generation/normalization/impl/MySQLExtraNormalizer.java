package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.transform.impl.CompositeIQTreeVariableGeneratorTransformer;

@Singleton
public class MySQLExtraNormalizer extends CompositeIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    private MySQLExtraNormalizer(OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer orderByNormalizer,
                                 ReplaceProvenanceConstantByNonGroundTermNormalizer provenanceNormalizer,
                                 ConvertValuesToUnionNormalizer toUnionNormalizer) {
        super(orderByNormalizer,
                provenanceNormalizer,
                toUnionNormalizer);
    }
}
