package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.transform.impl.CompositeIQTreeVariableGeneratorTransformer;

@Singleton
public class DB2ExtraNormalizer extends CompositeIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected DB2ExtraNormalizer(EnforceNullOrderNormalizer enforceNullOrderNormalizer,
                                 OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer projectOrderByNormalizer) {
        super(enforceNullOrderNormalizer,
                projectOrderByNormalizer);
    }
}
