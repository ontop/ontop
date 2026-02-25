package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;

public class OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected OnlyInPresenceOfDistinctPushProjectedOrderByTermsNormalizer(CoreSingletons coreSingletons) {
        super(new PushProjectedOrderByTermsTransformer(true, coreSingletons)::transform);
    }
}
