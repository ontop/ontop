package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;

public class AlwaysPushProjectedOrderByTermsNormalizer extends DialectExtraNormalizerBase {

    @Inject
    protected AlwaysPushProjectedOrderByTermsNormalizer(CoreSingletons coreSingletons) {
        super(new PushProjectedOrderByTermsTransformer(false, coreSingletons)::transform);
    }
}
