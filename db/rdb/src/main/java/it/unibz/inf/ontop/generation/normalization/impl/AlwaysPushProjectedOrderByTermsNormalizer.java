package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;

public class AlwaysPushProjectedOrderByTermsNormalizer extends PushProjectedOrderByTermsNormalizer {

    @Inject
    protected AlwaysPushProjectedOrderByTermsNormalizer(CoreSingletons coreSingletons) {
        super(false, coreSingletons);
    }
}
