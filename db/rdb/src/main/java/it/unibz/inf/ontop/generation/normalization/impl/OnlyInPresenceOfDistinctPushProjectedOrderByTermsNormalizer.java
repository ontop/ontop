package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;

public class OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer extends PushProjectedOrderByTermsNormalizer {

    @Inject
    protected OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer(IntermediateQueryFactory iqFactory, CoreSingletons coreSingletons) {
        super(true, iqFactory, coreSingletons);
    }
}
