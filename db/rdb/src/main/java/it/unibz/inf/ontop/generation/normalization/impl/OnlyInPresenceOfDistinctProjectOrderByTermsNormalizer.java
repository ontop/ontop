package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;

public class OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer extends ProjectOrderByTermsNormalizer {

    @Inject
    protected OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer(CoreSingletons coreSingletons) {
        super(true, coreSingletons);
    }
}
