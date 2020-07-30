package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.iq.transformer.DefinitionPushDownTransformer;
import it.unibz.inf.ontop.iq.transformer.ExplicitEqualityTransformer;
import it.unibz.inf.ontop.iq.transformer.TermTypeTermLiftTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * Accessible through Guice (recommended) or through OptimizationSingletons.
 */
public interface OptimizerFactory {

    ExplicitEqualityTransformer createEETransformer(VariableGenerator variableGenerator);

    TermTypeTermLiftTransformer createRDFTermTypeConstantTransformer(VariableGenerator variableGenerator);

    DefinitionPushDownTransformer createDefinitionPushDownTransformer(DefinitionPushDownRequest request);

}
