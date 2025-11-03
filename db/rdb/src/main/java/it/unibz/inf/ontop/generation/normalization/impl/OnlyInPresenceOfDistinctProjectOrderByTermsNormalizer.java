package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;

public class OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer(CoreSingletons coreSingletons) {
        super(IQTreeVariableGeneratorTransformer.of(
                vg -> new ProjectOrderByTermsTransformer(vg, true, coreSingletons)));
    }
}
