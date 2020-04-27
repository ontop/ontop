package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;

public class AlwaysProjectOrderByTermsNormalizer extends ProjectOrderByTermsNormalizer {

    @Inject
    protected AlwaysProjectOrderByTermsNormalizer(CoreSingletons coreSingletons) {
        super(false, coreSingletons);
    }
}
