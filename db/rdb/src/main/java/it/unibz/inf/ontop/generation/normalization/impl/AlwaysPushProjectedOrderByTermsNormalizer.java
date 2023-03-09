package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;

public class AlwaysPushProjectedOrderByTermsNormalizer extends PushProjectedOrderByTermsNormalizer {

    @Inject
    protected AlwaysPushProjectedOrderByTermsNormalizer(IntermediateQueryFactory iqFactory, CoreSingletons coreSingletons) {
        super(false, iqFactory, coreSingletons);
    }
}
