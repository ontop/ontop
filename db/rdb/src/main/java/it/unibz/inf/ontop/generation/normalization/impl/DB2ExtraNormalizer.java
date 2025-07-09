package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DB2ExtraNormalizer extends CompositeDialectNormalizer {

    @Inject
    protected DB2ExtraNormalizer(EnforceNullOrderNormalizer enforceNullOrderNormalizer,
                                 OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer projectOrderByNormalizer) {
        super(ImmutableList.of(
                enforceNullOrderNormalizer,
                projectOrderByNormalizer));
    }
}
