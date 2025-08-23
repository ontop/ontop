package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.iq.transformer.DefinitionPushDownTransformer;

/**
 * Accessible through Guice (recommended) or through OptimizationSingletons.
 */
public interface OptimizerFactory {

    DefinitionPushDownTransformer createDefinitionPushDownTransformer(DefinitionPushDownRequest request);

}
