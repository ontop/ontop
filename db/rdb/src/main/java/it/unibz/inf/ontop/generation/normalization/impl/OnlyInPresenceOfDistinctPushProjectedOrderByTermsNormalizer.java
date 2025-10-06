package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;

public class OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer extends DialectExtraNormalizerBase {

    @Inject
    protected OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer(CoreSingletons coreSingletons) {
        super(new PushProjectedOrderByTermsTransformer(true, coreSingletons)::transform);
    }
}
