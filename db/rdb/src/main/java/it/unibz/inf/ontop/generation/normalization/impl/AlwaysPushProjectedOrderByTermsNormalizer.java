package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;

public class AlwaysPushProjectedOrderByTermsNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected AlwaysPushProjectedOrderByTermsNormalizer(CoreSingletons coreSingletons) {
        super(new PushProjectedOrderByTermsTransformer(false, coreSingletons)::transform);
    }
}
