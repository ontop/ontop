package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.iq.transformer.ExplicitEqualityTransformer;
import it.unibz.inf.ontop.iq.transformer.MetaTermTypeTermLiftTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface OptimizerFactory {

    ExplicitEqualityTransformer createEETransformer(VariableGenerator variableGenerator);

    MetaTermTypeTermLiftTransformer createRDFTermTypeConstantTransformer(VariableGenerator variableGenerator);
}
