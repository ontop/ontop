package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;

public class OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer extends PushProjectedOrderByTermsNormalizer {

    @Inject
    protected OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer(CoreSingletons coreSingletons) {
        super(true, coreSingletons);
    }
}
